
CLASS_DIR = ./classes
SRC_DIR = ./src
JAVAC = javac -source 1.4 \-d $(CLASS_DIR) -sourcepath $(SRC_DIR) -classpath ${CLASSPATH}
MODULE = gt
EXTRAS = gt-config.n3
all: bin/viper-$(MODULE).jar

bin/viper-$(MODULE).jar: classfiles
	-mkdir bin
	-cp CONFIG/$(EXTRAS) classes
	cd classes;\
	touch temp.manifest;\
	echo Main-Class: edu.umd.cfar.lamp.viper.gui.core.RunGT >> temp.manifest;\
	if test 'which printf';\
	  then jar cmf temp.manifest ../bin/viper-$(MODULE).jar $(EXTRAS) `find ./ -name \*.class -exec printf "%s " "{}" \;` ; \
	  else jar cmf temp.manifest ../bin/viper-$(MODULE).jar $(EXTRAS) `find ./ -name \*.class -exec echo -n "{}" \;` ; \
	fi; \
	rm temp.manifest;\
	cd ..

classfiles: 
	-mkdir classes
	find $(SRC_DIR) -name \*.java | perl -pe "chop; s/^/ /;" > java_files
	$(JAVAC) @java_files
	rm java_files

clean:
	-rm -r classes
	-rm bin/*.jar
