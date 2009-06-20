#! /bin/csh -f

# This routine takes a raw file, a GTF/RDF file produces 4 output
# files corresponding to data which is MISSED, FALSE, DETECTED in RDF,
# and DETECTED in GTF respectivly

set basename = $1:r
echo "BASENAME: $basename"

set rawfile = $RAWPATH/$basename/$RAWFILE

set gtffile = $GTFPATH/$GTFFILE
set gtfattr = $GTFATTR
set gtftype = $GTFTYPE

set rdffile = $RDFPATH/$RDFFILE
set rdfattr = $RDFATTR
set rdftype = $RDFTYPE



echo " "
echo "*******************************************************"
echo "GetRawIds..."
echo " "
echo "COMMAND: GetRawIds.csh $rawfile"
GetRawIds.csh  $rawfile

echo " "
echo "*******************************************************"
echo "Filtering RDF..."
echo " "
foreach type (false detectrdf)
   echo "  creating $basename"
   filterVIPERObjects $rdffile $basename.$type $type.id
end

echo " "
echo "*******************************************************"
echo "Filtering GTF..."
echo " "
foreach type (missed detectgtf) 
   echo "  creating $basename.$type"
   filterVIPERObjects $gtffile $basename.$type $type.id
end

echo " "
echo "*******************************************************"
echo "Overlaying Data on Frames"
echo " "
filterVIPEROverlay $basename

echo "*******************************************************"
echo "Removing Extra Files"
echo " "
echo "...false.id missed.id detectgtf.id detectrdf.id tmp.txt"
echo "...*.false *.missed *.detectrdf *.detectgtf"
echo " "

rm false.id missed.id detectgtf.id detectrdf.id tmp.txt
rm *.false *.missed *.detectrdf *.detectgtf
