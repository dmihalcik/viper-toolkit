
CLASS_DIR = ./classes
SRC_DIR = ./src
JAVAC = javac -source 1.4 \-d $(CLASS_DIR) -sourcepath $(SRC_DIR) -classpath ${CLASSPATH}
CLASS_FILES = $(CLASS_DIR)/edu/umd/cfar/lamp/chronicle/ChronicleAdapter.class $(CLASS_DIR)/edu/umd/cfar/lamp/chronicle/ChronicleEvent.class $(CLASS_DIR)/edu/umd/cfar/lamp/chronicle/ChronicleListener.class $(CLASS_DIR)/edu/umd/cfar/lamp/chronicle/ChronicleModel.class $(CLASS_DIR)/edu/umd/cfar/lamp/chronicle/ChronicleViewer.class $(CLASS_DIR)/edu/umd/cfar/lamp/chronicle/ChronPanHandler.class $(CLASS_DIR)/edu/umd/cfar/lamp/chronicle/PBasicTimeSegment.class $(CLASS_DIR)/edu/umd/cfar/lamp/chronicle/PBasicTimeSegmentFactory.class $(CLASS_DIR)/edu/umd/cfar/lamp/chronicle/PTimeLineExpanderNode.class $(CLASS_DIR)/edu/umd/cfar/lamp/chronicle/PTimeLineHolder.class $(CLASS_DIR)/edu/umd/cfar/lamp/chronicle/PTimeLineStyle.class $(CLASS_DIR)/edu/umd/cfar/lamp/chronicle/PTimeLineView.class $(CLASS_DIR)/edu/umd/cfar/lamp/chronicle/PTimeSegmentFactory.class $(CLASS_DIR)/edu/umd/cfar/lamp/chronicle/PTimeSegmentFactoryFactory.class $(CLASS_DIR)/edu/umd/cfar/lamp/chronicle/TimeLine.class
JAVA_FILES = $(SRC_DIR)/edu/umd/cfar/lamp/chronicle/ChronicleAdapter.java $(SRC_DIR)/edu/umd/cfar/lamp/chronicle/ChronicleEvent.java $(SRC_DIR)/edu/umd/cfar/lamp/chronicle/ChronicleListener.java $(SRC_DIR)/edu/umd/cfar/lamp/chronicle/ChronicleModel.java $(SRC_DIR)/edu/umd/cfar/lamp/chronicle/ChronicleViewer.java $(SRC_DIR)/edu/umd/cfar/lamp/chronicle/ChronPanHandler.java $(SRC_DIR)/edu/umd/cfar/lamp/chronicle/PBasicTimeSegment.java $(SRC_DIR)/edu/umd/cfar/lamp/chronicle/PBasicTimeSegmentFactory.java $(SRC_DIR)/edu/umd/cfar/lamp/chronicle/PTimeLineExpanderNode.java $(SRC_DIR)/edu/umd/cfar/lamp/chronicle/PTimeLineHolder.java $(SRC_DIR)/edu/umd/cfar/lamp/chronicle/PTimeLineStyle.java $(SRC_DIR)/edu/umd/cfar/lamp/chronicle/PTimeLineView.java $(SRC_DIR)/edu/umd/cfar/lamp/chronicle/PTimeSegmentFactory.java $(SRC_DIR)/edu/umd/cfar/lamp/chronicle/PTimeSegmentFactoryFactory.java $(SRC_DIR)/edu/umd/cfar/lamp/chronicle/TimeLine.java
MODULE = chronicle
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
