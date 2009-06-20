#! /usr/bin/env python

import os
import os.path
import re
import string
import sys
import xml.dom.minidom

startAsmBraceRE = re.compile( r'__asm\s*\{' )
endAsmBraceRE = re.compile( r'\}' )
startAsmSingleRE = re.compile( r'__asm\s+(.+)$' )

for d in sys.argv[1:]:
    f = open( d )
    out = open( d + ".gas", "w" )
    for line in f:
        blockStart = startAsmBraceRE.subn( r'asm (".intel_syntax noprefix\\', line )
        if blockStart[1] > 0:
            line = blockStart[0]
            blockEnd = endAsmBraceRE.subn( '\\n");', line )
            while blockEnd[1] == 0:
                out.write(line)
		line = f.next()
		blockEnd = endAsmBraceRE.subn( r'\\n");', line )
		line = line[:-1] + "\\n\\\n"
	    else:
                line = blockEnd[0]
	else:
            line = startAsmSingleRE.sub( r'asm(".intel_syntax noprefix \\n \1\\n");', line )
        out.write(line)
