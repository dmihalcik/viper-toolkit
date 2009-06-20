
all: libs.jar

libs.jar: ./lib ./lib/combine-jars.sh ./lib/batik-awt-util.jar ./lib/batik-ext.jar \
./lib/batik-parser.jar ./lib/batik-svg-dom.jar ./lib/batik-svggen.jar \
./lib/batik-util.jar ./lib/dom.jar ./lib/jai_codec.jar ./lib/jai_core.jar \
./lib/jcommon-0.6.4.jar ./lib/jfreechart-0.9.2.jar ./lib/jmf.jar ./lib/junit.jar \
./lib/mlibwrapper_jai.jar  ./lib/xerces.jar ./lib/concurrent-1.3.0.jar \
./lib/db3-3-11.jar ./lib/icu4j.jar ./lib/jakarta-oro-2.0.5.jar ./lib/jena.jar \
./lib/log4j-1.2.7.jar ./lib/rdf-api-2001-01-19.jar ./lib/sesame-client.jar \
./lib/xercesImpl.jar ./lib/xmlParserAPIs.jar ./lib/concurrent.jar ./lib/junitperf.jar \
./lib/antlr.jar
	cd lib; \
	bash ./combine-jars.sh; \
	cd ..

clean:
	-rm libs.jar

