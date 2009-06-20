#!/bin/sh -v
mkdir temp
cd temp

for jarfile in ../*.jar ../*.zip
do
   jar -xf $jarfile
done


if test 'which printf'
  then find ./ \( -name \*.properties -o -name \*.class \) -exec printf "%s " "{}" \; > inputFiles
  else find ./ \( -name \*.properties -o -name \*.class \) -exec echo -n "{}" \; > inputFiles
fi
jar cf ../../libs.jar @inputFiles
rm inputFiles

cd ..
rm -r temp
