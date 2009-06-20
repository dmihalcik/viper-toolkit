#!/usr/bin/perl

if($#ARGV < 1){
 die "USAGE: removeDupes.pl <env. variable name> <path to add>";
}

if (not exists $ENV{$ARGV[0]}) {
   print $ARGV[1];
} elsif (not $ENV{$ARGV[0]} =~ /(^|:)$ARGV[1]($|:)/) {
   print $ARGV[1] . ':' . $ENV{$ARGV[0]};
} else {
   print $ENV{$ARGV[0]};
}
