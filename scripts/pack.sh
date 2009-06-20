#!/bin/sh

nd=`date +viper-%Y-%m-%d`

mkdir $nd
cd $nd
cp -r ../viper/* ./
rm *.config viper-cfg.sh
find ./ -name CVS -o -name core | xargs rm -r
cd ..
tar cf $nd.tar $nd
gzip $nd.tar
