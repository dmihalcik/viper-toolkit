# Makefile for ViPER:PE (aka gtfC).

# These should probably be set for your specific installation
GTFC_DIR = ${GTFC_HOME}
JAVAPATH = ${JAVA_HOME}



# 
DOC_DIR=$(GTFC_DIR)/docs/html
CLASS_DIR=$(GTFC_DIR)/classes
SRC_DIR=$(GTFC_DIR)/src
BIN_DIR=$(GTFC_DIR)/bin

# These should work, although it may vary depending on your compiler
# for jikes, you will probably have to add the rt.jar to your
#     classpath, and the Xdepend will be unnecessary.
# if you want optimization, specify '-O -g:none'
# if you want no JIT, pass -Djava.compiler=NONE
JAVAC = javac -d $(CLASS_DIR) -sourcepath $(SRC_DIR) -classpath ${CLASSPATH} -g
JRE = $(JAVAPATH)/java -cp ${CLASSPATH}
JAR = $(JAVAPATH)/jar
JAVADOC = $(JAVAPATH)/javadoc


CLASSES_WITHOUT_ROOTS = viper/comparison/CompEvaluator.class viper/comparison/CompFilter.class viper/comparison/CompMatrix.class viper/comparison/Comparison.class viper/comparison/EmptyFilter.class viper/comparison/GreedyFilter.class viper/comparison/MultipleMatchFilter.class viper/comparison/PrecisionRecall.class viper/comparison/SingleFilter.class viper/comparison/Surreal.class viper/comparison/TrackingInformation.class viper/comparison/ViperPE.class viper/descriptors/Converter.class viper/descriptors/DescAggregate.class viper/descriptors/DescHolder.class viper/descriptors/DescPrototype.class viper/descriptors/DescSingle.class viper/descriptors/DescVector.class viper/descriptors/Descriptor.class viper/descriptors/DescriptorConfigs.class viper/descriptors/DescriptorData.class viper/descriptors/DescriptorList.class viper/descriptors/EndOfBlockException.class   viper/descriptors/ImproperDescriptorException.class viper/descriptors/UnitTest.class viper/descriptors/attributes/AbstractAttribute.class viper/descriptors/attributes/Attribute.class viper/descriptors/attributes/AttributePrototype.class viper/descriptors/attributes/Attribute_bbox.class viper/descriptors/attributes/Attribute_bvalue.class viper/descriptors/attributes/Attribute_circle.class viper/descriptors/attributes/Attribute_dvalue.class viper/descriptors/attributes/Attribute_fvalue.class viper/descriptors/attributes/Attribute_lvalue.class viper/descriptors/attributes/Attribute_number.class viper/descriptors/attributes/Attribute_obox.class viper/descriptors/attributes/Attribute_point.class viper/descriptors/attributes/Attribute_polygon.class viper/descriptors/attributes/Attribute_svalue.class viper/descriptors/attributes/Attributes.class viper/descriptors/attributes/FrameSpan.class viper/descriptors/attributes/ImproperMetricException.class viper/descriptors/attributes/Attribute_ellipse.class  viper/reader/CountingStringTokenizer.class viper/reader/VReader.class viper/filters/LimitationsParser.class viper/filters/Rules.class viper/filters/Filterable.class viper/filters/RuleHolder.class viper/comparison/Evaluation.class viper/comparison/FramewiseEvaluation.class viper/comparison/TrackingEvaluation.class viper/descriptors/CanonicalFileDescriptor.class viper/comparison/ObjectEvaluation.class viper/comparison/ObjectInformation.class viper/descriptors/attributes/Measurable.class viper/comparison/distances/AttrDistance.class viper/comparison/distances/AttrMeasure.class viper/comparison/distances/DefaultMeasures.class  viper/comparison/distances/Distance.class viper/comparison/distances/Distances.class viper/comparison/distances/IgnoredValueException.class viper/comparison/distances/MeasureDistance.class viper/comparison/distances/UnknownDistanceException.class viper/descriptors/attributes/UncomposableException.class viper/descriptors/attributes/AttributeValue.class viper/descriptors/attributes/Composable.class viper/comparison/EvaluationParameters.class viper/descriptors/attributes/ExtendedAttributeValue.class viper/descriptors/attributes/ValueSpan.class viper/comparison/distances/DistanceHolder.class
ALL_CLASS_FILES = $(CLASS_DIR)/viper/comparison/CompEvaluator.class $(CLASS_DIR)/viper/comparison/CompFilter.class $(CLASS_DIR)/viper/comparison/CompMatrix.class $(CLASS_DIR)/viper/comparison/Comparison.class $(CLASS_DIR)/viper/comparison/EmptyFilter.class $(CLASS_DIR)/viper/comparison/GreedyFilter.class $(CLASS_DIR)/viper/comparison/MultipleMatchFilter.class $(CLASS_DIR)/viper/comparison/PrecisionRecall.class $(CLASS_DIR)/viper/comparison/SingleFilter.class $(CLASS_DIR)/viper/comparison/Surreal.class $(CLASS_DIR)/viper/comparison/TrackingInformation.class $(CLASS_DIR)/viper/comparison/ViperPE.class $(CLASS_DIR)/viper/descriptors/Converter.class $(CLASS_DIR)/viper/descriptors/DescAggregate.class $(CLASS_DIR)/viper/descriptors/DescHolder.class $(CLASS_DIR)/viper/descriptors/DescPrototype.class $(CLASS_DIR)/viper/descriptors/DescSingle.class $(CLASS_DIR)/viper/descriptors/DescVector.class $(CLASS_DIR)/viper/descriptors/Descriptor.class $(CLASS_DIR)/viper/descriptors/DescriptorConfigs.class $(CLASS_DIR)/viper/descriptors/DescriptorData.class $(CLASS_DIR)/viper/descriptors/DescriptorList.class $(CLASS_DIR)/viper/descriptors/EndOfBlockException.class $(CLASS_DIR)/viper/descriptors/ImproperDescriptorException.class $(CLASS_DIR)/viper/descriptors/UnitTest.class $(CLASS_DIR)/viper/descriptors/attributes/AbstractAttribute.class $(CLASS_DIR)/viper/descriptors/attributes/Attribute.class $(CLASS_DIR)/viper/descriptors/attributes/AttributePrototype.class $(CLASS_DIR)/viper/descriptors/attributes/Attribute_bbox.class $(CLASS_DIR)/viper/descriptors/attributes/Attribute_bvalue.class $(CLASS_DIR)/viper/descriptors/attributes/Attribute_circle.class $(CLASS_DIR)/viper/descriptors/attributes/Attribute_dvalue.class $(CLASS_DIR)/viper/descriptors/attributes/Attribute_fvalue.class $(CLASS_DIR)/viper/descriptors/attributes/Attribute_lvalue.class $(CLASS_DIR)/viper/descriptors/attributes/Attribute_number.class $(CLASS_DIR)/viper/descriptors/attributes/Attribute_obox.class $(CLASS_DIR)/viper/descriptors/attributes/Attribute_point.class $(CLASS_DIR)/viper/descriptors/attributes/Attribute_polygon.class $(CLASS_DIR)/viper/descriptors/attributes/Attribute_svalue.class $(CLASS_DIR)/viper/descriptors/attributes/Attributes.class $(CLASS_DIR)/viper/descriptors/attributes/FrameSpan.class $(CLASS_DIR)/viper/descriptors/attributes/ImproperMetricException.class $(CLASS_DIR)/viper/descriptors/attributes/Attribute_ellipse.class $(CLASS_DIR)/viper/reader/CountingStringTokenizer.class $(CLASS_DIR)/viper/reader/VReader.class $(CLASS_DIR)/viper/filters/LimitationsParser.class $(CLASS_DIR)/viper/filters/Rules.class $(CLASS_DIR)/viper/filters/Filterable.class $(CLASS_DIR)/viper/filters/RuleHolder.class ${CLASS_DIR}/viper/comparison/Evaluation.class ${CLASS_DIR}/viper/comparison/FramewiseEvaluation.class ${CLASS_DIR}/viper/comparison/TrackingEvaluation.class $(CLASS_DIR)/viper/descriptors/CanonicalFileDescriptor.class ${CLASS_DIR}/viper/comparison/ObjectEvaluation.class ${CLASS_DIR}/viper/comparison/ObjectInformation.class $(CLASS_DIR)viper/descriptors/attributes/Measurable.class $(CLASS_DIR)/viper/comparison/distances/AttrDistance.class $(CLASS_DIR)/viper/comparison/distances/AttrMeasure.class $(CLASS_DIR)/viper/comparison/distances/DefaultMeasures.class  $(CLASS_DIR)/viper/comparison/distances/Distance.class $(CLASS_DIR)/viper/comparison/distances/Distances.class $(CLASS_DIR)/viper/comparison/distances/IgnoredValueException.class $(CLASS_DIR)/viper/comparison/distances/MeasureDistance.class $(CLASS_DIR)/viper/comparison/distances/UnknownDistanceException.class $(CLASS_DIR)/viper/descriptors/attributes/UncomposableException.class $(CLASS_DIR)/viper/descriptors/attributes/AttributeValue.class $(CLASS_DIR)/viper/descriptors/attributes/Composable.class $(CLASS_DIR)/viper/comparison/EvaluationParameters.class $(CLASS_DIR)/viper/descriptors/attributes/ExtendedAttributeValue.class $(CLASS_DIR)/viper/descriptors/attributes/ValueSpan.class $(CLASS_DIR)/viper/comparison/distances/DistanceHolder.class
ALL_JAVA_FILES = $(SRC_DIR)/viper/comparison/CompEvaluator.java $(SRC_DIR)/viper/comparison/CompFilter.java $(SRC_DIR)/viper/comparison/CompMatrix.java $(SRC_DIR)/viper/comparison/Comparison.java $(SRC_DIR)/viper/comparison/EmptyFilter.java $(SRC_DIR)/viper/comparison/GreedyFilter.java $(SRC_DIR)/viper/comparison/MultipleMatchFilter.java $(SRC_DIR)/viper/comparison/PrecisionRecall.java $(SRC_DIR)/viper/comparison/SingleFilter.java $(SRC_DIR)/viper/comparison/Surreal.java $(SRC_DIR)/viper/comparison/TrackingInformation.java $(SRC_DIR)/viper/comparison/ViperPE.java $(SRC_DIR)/viper/descriptors/Converter.java $(SRC_DIR)/viper/descriptors/DescAggregate.java $(SRC_DIR)/viper/descriptors/DescHolder.java $(SRC_DIR)/viper/descriptors/DescPrototype.java $(SRC_DIR)/viper/descriptors/DescSingle.java $(SRC_DIR)/viper/descriptors/DescVector.java $(SRC_DIR)/viper/descriptors/Descriptor.java $(SRC_DIR)/viper/descriptors/DescriptorConfigs.java $(SRC_DIR)/viper/descriptors/DescriptorData.java $(SRC_DIR)/viper/descriptors/DescriptorList.java $(SRC_DIR)/viper/descriptors/EndOfBlockException.java $(SRC_DIR)/viper/descriptors/ImproperDescriptorException.java $(SRC_DIR)/viper/descriptors/UnitTest.java $(SRC_DIR)/viper/descriptors/attributes/AbstractAttribute.java $(SRC_DIR)/viper/descriptors/attributes/Attribute.java $(SRC_DIR)/viper/descriptors/attributes/AttributePrototype.java $(SRC_DIR)/viper/descriptors/attributes/Attribute_bbox.java $(SRC_DIR)/viper/descriptors/attributes/Attribute_bvalue.java $(SRC_DIR)/viper/descriptors/attributes/Attribute_circle.java $(SRC_DIR)/viper/descriptors/attributes/Attribute_dvalue.java $(SRC_DIR)/viper/descriptors/attributes/Attribute_fvalue.java $(SRC_DIR)/viper/descriptors/attributes/Attribute_lvalue.java $(SRC_DIR)/viper/descriptors/attributes/Attribute_number.java $(SRC_DIR)/viper/descriptors/attributes/Attribute_obox.java $(SRC_DIR)/viper/descriptors/attributes/Attribute_point.java $(SRC_DIR)/viper/descriptors/attributes/Attribute_polygon.java $(SRC_DIR)/viper/descriptors/attributes/Attribute_svalue.java $(SRC_DIR)/viper/descriptors/attributes/Attributes.java $(SRC_DIR)/viper/descriptors/attributes/FrameSpan.java $(SRC_DIR)/viper/descriptors/attributes/ImproperMetricException.java $(SRC_DIR)/viper/descriptors/attributes/Attribute_ellipse.java $(SRC_DIR)/viper/reader/CountingStringTokenizer.java $(SRC_DIR)/viper/reader/VReader.java $(SRC_DIR)/viper/filters/LimitationsParser.java $(SRC_DIR)/viper/filters/Rules.java $(SRC_DIR)/viper/filters/Filterable.java $(SRC_DIR)/viper/filters/RuleHolder.java ${SRC_DIR}/viper/comparison/Evaluation.java ${SRC_DIR}/viper/comparison/FramewiseEvaluation.java ${SRC_DIR}/viper/comparison/TrackingEvaluation.java $(SRC_DIR)/viper/descriptors/CanonicalFileDescriptor.java ${SRC_DIR}/viper/comparison/ObjectEvaluation.java ${SRC_DIR}/viper/comparison/ObjectInformation.java $(SRC_DIR)/viper/descriptors/attributes/Measurable.java $(SRC_DIR)/viper/comparison/distances/AttrDistance.java $(SRC_DIR)/viper/comparison/distances/AttrMeasure.java $(SRC_DIR)/viper/comparison/distances/DefaultMeasures.java  $(SRC_DIR)/viper/comparison/distances/Distance.java $(SRC_DIR)/viper/comparison/distances/Distances.java $(SRC_DIR)/viper/comparison/distances/IgnoredValueException.java $(SRC_DIR)/viper/comparison/distances/MeasureDistance.java $(SRC_DIR)/viper/comparison/distances/UnknownDistanceException.java $(SRC_DIR)/viper/descriptors/attributes/UncomposableException.java $(SRC_DIR)/viper/descriptors/attributes/AttributeValue.java $(SRC_DIR)/viper/descriptors/attributes/Composable.java $(SRC_DIR)/viper/comparison/EvaluationParameters.java $(SRC_DIR)/viper/descriptors/attributes/ExtendedAttributeValue.java $(SRC_DIR)/viper/descriptors/attributes/ValueSpan.java $(SRC_DIR)/viper/comparison/distances/DistanceHolder.java

TESTFILES = $(SRC_DIR)/viper/descriptors/UnitTest.java 

# These variables are used in creating the javadoc.
WINDOWTITLE='GTFC 1.1 Specification'
DOCTITLE='Ground Truth File Comparison Specification'
HEADER='<b>GtfC 1.1</b>'
BOTTOM='<small><a href="http://groups-beta.google.com/group/viper-toolkit">Discuss</a></small>'


all: $(SRC_DIR)/viper/comparison/mainClass.manifest classfiles
	cd classes;\
	touch temp.manifest;\
	echo Main-Class: viper.comparison.ViperPE >> temp.manifest;\
	if test 'which printf';\
	  then jar cmf temp.manifest ../bin/viper-pe.jar `find ./ -name \*.class -exec printf "%s " "{}" \;` ; \
	  else jar cmf temp.manifest ../bin/viper-pe.jar `find ./ -name \*.class -exec echo -n "{}" \;` ; \
	fi;\
	cd ..


test:
classfiles: 
	-mkdir $(CLASS_DIR)
	find $(SRC_DIR) -name \*.java | perl -pe "chop; s/^(.*)/\1 /;" > java_files
	$(JAVAC) @java_files
	rm java_files

runTestDesc:
	$(JRE) -Djava.compiler=NONE test.ui.TestRunner viper.descriptors.UnitTest


javadoc:	
	-mkdir $(DOC_DIR)
	$(JAVADOC) -sourcepath $(SRC_DIR) -d $(DOC_DIR) -use -windowtitle $(WINDOWTITLE) -doctitle $(DOCTITLE) -header $(HEADER) -bottom $(BOTTOM) -overview $(SRC_DIR)/viper/comparison/overview.html @packages

docsclean:
	-rm -r $(DOC_DIR)

clean:
	-rm -r $(CLASS_DIR)
	-rm $(BIN_DIR)/*.jar
