#!/bin/sh -v
mkdir temp
cp "$1" temp
cd temp
jar xf "$1"
mv "$1" "../$1.old"
if test 'which printf'
  then jar cmf "../$2" "../$1" `find ./ -name \*.class -exec printf "%s " "{}" \;`
  else jar cmf "../$2" "../$1" `find ./ -name \*.class -exec echo -n "{}" \;`
fi
cd ..
rm -r temp
