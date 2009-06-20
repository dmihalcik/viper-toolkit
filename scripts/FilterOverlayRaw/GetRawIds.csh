#!/usr/bin/csh

# This routine checks the raw files and extracts all of the object
# numbers for either MISSING FALSE OR DETECT and returns outputs
# the results to a set of files missed.id, false.id, detectrdf.id,
# detectgtf.id

echo "####################################################"
echo "Processing: $1"

if ($1x == 'x') then
    print "Usage: GetRAWIDs.csh <rawfile>"
endif

#
# Get the data from the .raw files
#

egrep "DETECT|FALSE|MISSED" $1 | sed -e "s/, /,/g" | cut -d ' ' -f2,3,7 | tr '\[' ' ' | tr '\]' ' ' >! tmp.txt


#
# FALSE
#


grep "FALSE" tmp.txt | sed -e "s/^ //" | cut -d ' ' -f1 | tr ',' '\012' | sort -n  | tr "\012" " " >! false.id

echo " "
echo "FALSE:"
cat false.id | tr "\012" " "
echo " "

#
# MISSED
#
grep "MISSED" tmp.txt | sed -e "s/^ //" | cut -d ' ' -f1 | tr ',' '\012' | sort -n  | tr "\012" " ">! missed.id

echo " "
echo "MISSED:" 
cat missed.id | tr "\012" " "
echo " "

#
# DETECTED - GTF
#

grep "DETECT" tmp.txt | sed -e "s/^ //" | cut -d ' ' -f1 | tr ',' '\012' | sort -n |  tr "\012" " " >! detectgtf.id

echo " "
echo "DETECTED - GTF: "
cat detectgtf.id | tr "\012" " "
echo " "

#
# DETECTED - RDF
#

grep "DETECT" tmp.txt | sed -e "s/^ //" | sed -e "s/   / /g" | sed -e "s/  / /g" | cut -d ' ' -f3 | tr ',' '\012' | sort -n |  tr "\012" " " >! detectrdf.id

echo " "
echo "DETECTED - RDF: " 
cat detectrdf.id | tr "\012" " "
echo " "















