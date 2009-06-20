#!/usr/bin/perl

if($#ARGV != 0){
 die "USAGE: removeDupes.pl <env. variable name>";
}

if (not exists $ENV{$ARGV[0]}) {
   print '.';
   exit 0;
}
@cp = sort split /:/, $ENV{$ARGV[0]};
@newcp = @cp[0..0];
for (@cp) {
  push @newcp, $_ if ($newcp[-1] ne $_);
}
print join ':', @newcp;
