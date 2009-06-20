#!/fs/lamp/usr/local/bin/python
from __future__ import generators

import os
import os.path
import string

def find(dir):
    "Iterates over all files in dir and its subdirectories"
    for f in os.listdir(dir):
        nf = dir + "/" + f
        if os.path.isdir(nf):
            for descendant in find (nf):
                yield descendant
        elif os.path.isfile(nf):
            yield nf

def find_these(dir, filter_func):
    for d in find (dir):
        if filter_func(d):
            yield d

def make_makefile(sub_dir, module_name):
    curr_dir = viper_dir + "/" + sub_dir
    java_filter = lambda s: len(s)>5 and s[-5:] == ".java"
    classFiles, javaFiles = ([],[])
    root_dir = curr_dir + "/src"
    os.chdir(root_dir)
    for f in find_these(".", java_filter):
        classFiles += ["$(CLASS_DIR)" + f[1:-5]+".class"]
        javaFiles += ["$(SRC_DIR)"+ f[1:]]
    makefile = open(curr_dir + "/makefile", 'w')
    makefile.write("""
CLASS_DIR = ./classes
SRC_DIR = ./src
JAVAC = javac -source 1.4 \-d $(CLASS_DIR) -sourcepath $(SRC_DIR) -classpath ${CLASSPATH}
CLASS_FILES = %s
JAVA_FILES = %s
MODULE = %s
all: bin/$(MODULE).jar

bin/$(MODULE).jar: classfiles
\t-mkdir bin
\tcd classes;\\
\t if test 'which printf';\\
\t  then jar cmf ../src/mainClass.manifest ../bin/$(MODULE).jar `find ./ -name \*.class -exec printf "%%s " "{}" \\;` ; \\
\t  else jar cmf ../src/mainClass.manifest ../bin/$(MODULE).jar `find ./ -name \*.class -exec echo -n "{}" \\;` ; \\
\t fi; \\
\t cd ..

classfiles: 
\t-mkdir classes
\t$(JAVAC) $(JAVA_FILES)

clean:
\t-rm -r classes/*
"""%(string.join(classFiles), string.join(javaFiles), module_name))
    makefile.close()

if __name__ == '__main__':
    viper_dir = "C:/Program Files/eclipse/workspace/viper"
    mods = [("api", "api"), ("apploader", "apploader"), 
            ("chronicle", "chronicle"), ("jmpeg", "jmpeg"), 
            ("gt", "viper-gt"),]
    for (sub_dir, module_name) in mods:
        make_makefile(sub_dir, module_name)