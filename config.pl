#!/usr/bin/perl

# Usage: config.pl
#  -- must be used from the directory where viper is installed.
#
# A script to set up the root viper.config that all of the 
# applications rely on.
#

sub getDir {
   open( INFO, "which $_[0] |" ) or die "which fails to determine location of $_[0]: $!\n";
   $path = <INFO> or die "which does not determine $_[0] directory: $!\n";
   chop( $path );
   if ($path =~ /aliased to/) {
      $path =~ s/^.*?aliased to \//\//;
   }
   if (not $path =~ /^\//
       or not $path=~ /${_[0]}$/) {
      die "Cannot determine $_[0] directory: $path";
   }
   $path =~ s/\/${_[0]}$//;
   close( INFO );
   return $path;
}

open( INFO, "pwd |" ) or die "Cannot determine current directory: $!\n";
$root = <INFO> or die "Cannot determine current directory: $!\n";
chomp $root;
close( INFO );


$javapath = getDir( "java" );
$perlpath = getDir( "perl" );
$tclshpath = getDir( "tclsh" );

# Make the megalib jar "libs.jar"
`cd common; make; cd ..`;

open( CFG_FILE, '>viper.config' ) or die "Cannot open viper.config: $!\n";
print CFG_FILE << "EOF";

setenv VIPER_ROOT "${root}"
setenv JAVA_HOME "${javapath}"
setenv JAVA  "${javapath}/java"

setenv PERL_HOME "${perlpath}"
setenv TKL_HOME "${tclshpath}"

setenv PATH `"${perlpath}/perl" "\${VIPER_ROOT}/scripts/addPath.pl" "PATH" "\${PERL_HOME}"`

setenv CLASSPATH `"${perlpath}/perl" "\${VIPER_ROOT}/scripts/addPath.pl" "CLASSPATH" "\${VIPER_ROOT}/common/libs.jar"`
setenv CLASSPATH `"${perlpath}/perl" "\${VIPER_ROOT}/scripts/addPath.pl" "CLASSPATH" "\${VIPER_ROOT}/common/lib/jai_core.jar"`
setenv CLASSPATH `"${perlpath}/perl" "\${VIPER_ROOT}/scripts/addPath.pl" "CLASSPATH" "\${VIPER_ROOT}/common/lib/jai_codec.jar"`
setenv LD_LIBRARY_PATH `"${perlpath}/perl" "\${VIPER_ROOT}/scripts/addPath.pl" "LD_LIBRARY_PATH" "\${VIPER_ROOT}/common/lib"`

foreach module (api apploader chronicle jmpeg gt pe)
   setenv CLASSPATH `"${perlpath}/perl" "\${VIPER_ROOT}/scripts/addPath.pl" "CLASSPATH" "\${VIPER_ROOT}/\${module}/bin/viper-\${module}.jar"`
end

source "\${VIPER_ROOT}/gt/ViPER-GT.config"
source "\${VIPER_ROOT}/pe/ViPER-PE.config"
source "\${VIPER_ROOT}/viz/ViPER-Viz.config"
source "\${VIPER_ROOT}/scripts/ViPER-scripts.config"

EOF

close( CFG_FILE );


open( CFG_FILE, '>viper-cfg.sh' ) or die "Cannot open viper-cfg.sh: $!\n";
print CFG_FILE << "EOF";
set -a

VIPER_ROOT="${root}"
JAVA_HOME="${javapath}"
JAVA="${javapath}/java"

PERL_HOME="${perlpath}"
TKL_HOME="${tclshpath}"

PATH=`"${perlpath}/perl" "\${VIPER_ROOT}/scripts/addPath.pl" "PATH" "\${PERL_HOME}"`

CLASSPATH=`"${perlpath}/perl" "\${VIPER_ROOT}/scripts/addPath.pl" "CLASSPATH" "\${VIPER_ROOT}/common/libs.jar"`
CLASSPATH=`"${perlpath}/perl" "\${VIPER_ROOT}/scripts/addPath.pl" "CLASSPATH" "\${VIPER_ROOT}/common/lib/jai_core.jar"`
CLASSPATH=`"${perlpath}/perl" "\${VIPER_ROOT}/scripts/addPath.pl" "CLASSPATH" "\${VIPER_ROOT}/common/lib/jai_codec.jar"`
LD_LIBRARY_PATH=`"${perlpath}/perl" "\${VIPER_ROOT}/scripts/addPath.pl" "LD_LIBRARY_PATH" "\${VIPER_ROOT}/common/lib"`

for module in api apploader chronicle jmpeg gt pe
do
   CLASSPATH=`"${perlpath}/perl" "\${VIPER_ROOT}/scripts/addPath.pl" "CLASSPATH" "\${VIPER_ROOT}/\${module}/bin/viper-\${module}.jar"`
done

for module in gt pe viz scripts
do
   . "\${VIPER_ROOT}/\${module}/ViPER-\${module}-cfg.sh"
done

EOF

close( CFG_FILE );

