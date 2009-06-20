#!/bin/sh

if [ $# -ne 1 ]; then
    echo 1>&2 Usage: $0 filename.mpg
    exit 127
fi

case $1 in
    *.mpg)  x=`basename $1 .mpg`;;
    *.mpeg) x=`basename $1 .mpeg`;;
    *)      x=$1
esac

mpeg_play -no_display -dither ppm $1
cat >> $x.info <<EOF
#VIPER_VERSION_3.0
1
EOF

ls *.ppm >> $x.info

