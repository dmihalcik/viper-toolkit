
CLASS_DIR = ./classes
SRC_DIR = ./src
JAVAC = javac -source 1.4 \-d $(CLASS_DIR) -sourcepath $(SRC_DIR) -classpath ${CLASSPATH}
MODULE = jmpeg
all: bin/viper-$(MODULE).jar

bin/viper-$(MODULE).jar: classfiles
	-mkdir bin
	cd classes;\
	 if test 'which printf';\
	  then jar cf ../bin/viper-$(MODULE).jar `find ./ -name \*.class -exec printf "%s " "{}" \;` ; \
	  else jar cf ../bin/viper-$(MODULE).jar `find ./ -name \*.class -exec echo -n "{}" \;` ; \
	 fi; \
	 cd ..

classfiles: 
	-mkdir classes
	find $(SRC_DIR) -name \*.java | perl -pe "chop; s/^/ /;" > java_files
	$(JAVAC) @java_files
	rm java_files 

clean:
	-rm -r classes
	-rm bin/*.jar
