#!/usr/bin/perl

# Scott Mennealy mennealy@cfar.umd.edu
# 11/12/99

if(("x".$ARGV[1]) eq "x"){
 die "USAGE: RunEvaluation [-noeval] <data_file> <template_file> ... died";
}

$gtfFile = "";
$rdfFile = "";
$prFile = "";
$epfFile = "";
$runName = "";
$graphName = "";
$resultsDir = "";
@origStringList = ();
@newStringList = ();
@templateData = ();
@graphicList = ();
@fields = ();
@graphicNameList = ();
$includeTotal=0;
$graphicTotal=0;
$flag = 0;
@includeTabulator = ();
$noEval = 0;

$MAX_INCLUDES = 25;

initialize_Include_Tab($MAX_INCLUDES);

if( $ARGV[0] =~ /^-noeval/ ) {
    $noEval = true;
    $ARGV[0] = $ARGV[1]; $ARGV[1] = $ARGV[2];
} else {
    $noEval = false;
}

$dataFile = $ARGV[0];
open (DATAFILE, $dataFile) || die "ERROR: Can not open $dataFile: $!";

$templateFile = $ARGV[1];
open (TEMPLATEFILE, $templateFile) || 
     	die "ERROR: Can not open $templateFile: $!";
@templateData = <TEMPLATEFILE>;

################################
# GET FIRST LINE FROM DATAFILE #
################################
     $marker = <DATAFILE>;
     @fields = parseLine( $marker );

##########################################
# BIG LOOP TO CHECK FOR END OF DATAFILE  #
##########################################

  while($fields[0] ne '#END'){

     if($fields[0] eq '#RUN_EVAL'){
	 if(0 == $noEval) {
	     buildDir($runName);
	     makeEPF(@templateData);
	     runEval();
	 }
     } 
     elsif($fields[0] eq '$DATA_HOME'){
         $ENV{DATA_HOME} = $fields[1];
     }
     elsif($fields[0] eq '$EVAL_HOME'){
         $ENV{EVAL_HOME} = $fields[1];
     }
     elsif($fields[0] eq '$EVAL_GTF'){
	 @result = divide($fields[1]);
         $ENV{EVAL_GTF} = $result[0].'/'.$result[1];  
     }
     elsif($fields[0] eq '$EVAL_RDF'){
	 @result = divide($fields[1]);
         $ENV{EVAL_RDF} = $result[0].'/'.$result[1];  
     }
     elsif($fields[0] eq '$EVAL_RESULTS'){ 
	 @result = divide($fields[1]);
         $ENV{EVAL_RESULTS} = $result[0].'/'.$result[1];  
     }
     elsif($fields[0] eq '$EVAL_EPF'){
         @result = divide($fields[1]);
         $ENV{EVAL_EPF} = $result[0].'/'.$result[1];  
     }
     elsif($fields[0] eq '$GTF'){
         $gtfFile = $fields[1]; 
     }
     elsif($fields[0] eq '$RDF'){
	 $rdfFile = $fields[1];
     }
     elsif($fields[0] eq '$PR'){
	 $prFile = $fields[1];
     }
     elsif($fields[0] eq '$NAME'){
	 $runName = $fields[1];
     }
     elsif($fields[0] eq '*'){	
	$newString = $fields[2];
        $newLength = $#fields - 3;
        for($k=1; $k<=$newLength; $k++){
                $newString = $newString." $fields[$k+3]";
        }
	setParameters($fields[1],$newString);
     }
     elsif($fields[0] eq '#RUN_GRAPH'){
	  runGraphs();	 
     }
     elsif($fields[0] eq '#NAME_GRAPH'){
	$graphName = $fields[1];
     }
     elsif($fields[0] eq '#INCLUDE_GRAPH'){
	$includeTotal=0;
	if($flag != 0){
		$graphicTotal++;
	}
	if($graphName eq ""){	
		$graphName=$runName;
	}
	 print "ADDING $runName to graphicList[$graphicTotal][$includeTotal]\n";
	 $graphicList[$graphicTotal][$includeTotal] = $runName;
	 push(@graphicNameList,$graphName);	
	 $graphName = "";
	 $includeTotal = 1;
	 $includeTabulator[$graphicTotal]++;
	 $flag = 1;
     }
     elsif($fields[0] eq '#CLEAR_GRAPH'){
 	 @graphicNameList = ();
	 @graphicList = ();
	 initialize_Include_Tab($MAX_INCLUDES);
	 $graphicTotal=0;
	 $includeTotal=0;
	 $flag=0;
     }
     elsif($fields[0] eq '#APPEND_GRAPH'){
	$graphicList[$graphicTotal][$includeTotal] =$runName;
	$includeTotal++;
	$includeTabulator[$graphicTotal]++;
     }
     elsif($fields[0] eq '#RUN_COMBINED_GRAPHS'){
	  runAllGraphs();	 
     }
     elsif(($fields[0] eq "")||($fields[0] eq "!!")){
	 #nothing happens
     }
     else{
         die "ERROR: $fields[0] is not a valid specifier. Died";
     }
     $marker = <DATAFILE>;
     @fields = parseLine( $marker );
  }
######################################
# END OF DATAFILE IS REACHED         #
# COMPUTE FOR LAST RUN               #
######################################

          close DATAFILE;
	  close TEMPLATEFILE;

sub setParameters{
  my $length = $#origStringList + 1;
  my $orig = $_[0];
  my $new = $_[1];
  my $flag = 0;
###########################################################
#  CHECK TO SEE IF ORIGSTRING IS ALREADY IN origARRAY     #
#  IF IT IS, DON'T NEED TO ADD A NEW ELEMENT TO origARRAY #
#  ONLY  newARRAY, IF NOT FOUND ADD TO BOTH               #
###########################################################
   for($i=0; $i<$length; $i++){
       if($orig eq $origStringList[$i]){
	  $newStringList[$i] = $new;
	  $flag=1;
       }
   }
    if($flag==0){
	push(@origStringList,$orig);
	push(@newStringList,$new);
    }
 }
sub buildDir{
    mkdir("$ENV{EVAL_RESULTS}/$_[0]",0755);
    $resultsDir="$ENV{EVAL_RESULTS}/$_[0]";
   }
sub makeEPF{
    my @values, $templateLength,$paramLength;
    @values = @_;
    $templateLength = $#values;
    $paramLength = $#origStringList + 1;
################################################   
# MAKE CHANGES TO TEMPLATE AND STORE IN VALUES #
################################################
  for($i=0; $i<$templateLength; $i++){
      for($j=0; $j<$paramLength; $j++){
	  $values[$i] =~ s/$origStringList[$j]/$newStringList[$j]/g;  
      }
  } 
###################################################
# PUT NEWLY CREATED EPF FILE IN RESULTS DIRECTORY #
###################################################
    $epfFile = $runName.".epf";
    print "$ENV{EVAL_EPF}\n";
    open(OUT,">$ENV{EVAL_EPF}/$epfFile") || die "ERROR: Did you setup the Environment?\n   Can not create $ENV{EVAL_EPF}/$epfFile:\n   $!";
    print OUT @values;
    close OUT;
 }

sub runEval{
    chdir $resultsDir;
    my $logfile = $runName.".log";
    my $rawfile = $runName.".raw";
    my $outfile = $runName.".out";
########################
# DO ACTUAL EVALUATION #
######################## 
    my @command = "viper-pe -pr $ENV{EVAL_PROP}/$prFile -epf $ENV{EVAL_EPF}/$epfFile -g $ENV{EVAL_GTF}/$gtfFile -r $ENV{EVAL_RDF}/$rdfFile -o $outfile -raw $rawfile -l -";
   print "\n###############################################\n";
   print "Running Evaluation Command.....\n\n";
   print "COMMAND: @command\n";
   print "##############################################\n";
   system(@command);
   $exit_value  = $? >> 8;
   $signal_num  = $? & 127;
   $dumped_core = $? & 128;  
   if( $exit_value != 0 ) {
     print "\n viper-pe Failed: Exit value $exit_value\n";
   }
   if( $signal_num != 0 ) {
     print "\n viper-pe received signal: $signal_num\n";
   }
   if( $dumped_core != 0 ) {
     print "\n viper-pe dumped core!!!\n";
   }
}

sub runGraphs{
    $resultsDir = "$ENV{EVAL_RESULTS}/$runName";
    chdir $resultsDir;
    my $configName="$runName".".config";
    open(CONFIG,">$configName");
    print CONFIG "1 $runName\n";
    print CONFIG "$runName".".raw\n";
    print CONFIG "$runName\n";
    print CONFIG "$runName\n";
    print CONFIG "\@date";

    close CONFIG;

    print "\nMAKING GRAPH.....\n";
    print "COMMAND: makeGraph.pl $configName\n";
    system("makeGraph.pl $configName");
}

sub runAllGraphs{
    my $graphicTotal = $#graphicList + 1;
    $newString = $ENV{EVAL_HOME}; 
    my $combinedRunName = <DATAFILE>;
    chomp($combinedRunName);

    buildDir($combinedRunName);
    chdir $resultsDir;
    my $configName = "$combinedRunName".".config";
    open(CONFIG,">$configName");
    print CONFIG "$graphicTotal $combinedRunName\n";

    print "RUNNING ALL GRAPHS: ";
    print "$graphicList[0][0] \n";
    print "$graphicList[0][1] \n";
    print "$graphicList[0][2] \n";

   $newline=1;

    for($i=0; $i<$graphicTotal; $i++){
     for($k=0; $k<$includeTabulator[$i]; $k++){
	print CONFIG "$ENV{EVAL_RESULTS}/$graphicList[$i][$k]/$graphicList[$i][$k]".".raw";

	if(($k+1)!=$includeTabulator[$i]){
		print CONFIG ", ";
	}
	else{
		print CONFIG "\n";
	        $newline=0;
	}
     }
    }
     if($newline == 1){
        print CONFIG "\n";
     }
    for($j=0; $j<$graphicTotal; $j++){
	print CONFIG "$graphicNameList[$j]\n";
    }

    print CONFIG "$combinedRunName\n";   
    print CONFIG "\@date";
    
    close CONFIG;
    print "\nMAKING COMBINED GRAPHS.....\n";
    print "makeGraph.pl $configName\n";
    system("makeGraph.pl $configName");
}
sub deleteSpaces{
 for($i=0; $i<($#fields+1); $i++){
   $fields[i] =~ s/" "/""/g;  
 }
}
sub divide{
   $root = "";
   $tail = "";
   $count = 1;

   @fields = split(/\//,$_[0]);

   if($fields[0] eq '$EVAL_HOME'){
      $root = $ENV{EVAL_HOME}; 
   }
   elsif($fields[0] eq '$DATA_HOME'){
      $root = $ENV{DATA_HOME}; 
   }
   while($count < ($#fields + 1)){
     if($count != 1){
       $tail = $tail.'/'.$fields[$count];
     }
     else{
	 $tail = $fields[$count];
     }
       $count++;
   }

   return ($root,$tail);
}

sub parseLine{
    chomp( $_[0] );

    @rets = ();
    if ($_[0] =~ /^\s*(#NAME_GRAPH) (.*)$/) {
        $rets[0] = "#NAME_GRAPH";
        $rets[1] = $2;
    } elsif ($_[0] =~ /^\s*\*\s*(<\w+>)\s*=\s*(.*)$/) {
        $rets[0] = "*";
        $rets[1] = $1;
        $rets[2] = $2;
    } else {
        $_[0] =~ s/ //g;
        @rets = split(/=/, $_[0]);
    }
    return @rets;
}

sub initialize_Include_Tab{
  my $max = $_[0];

  for($i=0;$i<$max;$i++){
	$includeTabulator[$i]=0;		
  }
}
