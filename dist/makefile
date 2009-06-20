all: viper-lite.tar.gz

viper-lite.tar.gz: viper-gt.jar viper-pe.jar  viper-cfg.jar README.txt
	tar Fcf viper-lite.tar viper-pe.jar viper-gt.jar viper-cfg.jar README.txt lib/* -C ../gt resources -C .. common/lib -C ../gt/CONFIG gt-config.n3 -C ../gt/CONFIG gtc-config.n3
	gzip viper-lite.tar

viper-cfg.jar: bin extjars viperjars
	touch temp.manifest
	echo Manifest-Version: 1.0 >> temp.manifest
	echo Main-Class: edu.umd.cfar.lamp.viper.gui.core.RunConfigEditor >> temp.manifest
	echo "Class-Path: . \\ " >> temp.manifest
	cat < viperjars >> temp.manifest
	cat < extjars >> temp.manifest
	jar cmf temp.manifest viper-cfg.jar
	rm temp.manifest

viper-gt.jar: bin extjars viperjars
	touch temp.manifest
	echo Manifest-Version: 1.0 >> temp.manifest
	echo Main-Class: edu.umd.cfar.lamp.viper.gui.core.RunGT >> temp.manifest
	echo "Class-Path: . \\ " >> temp.manifest
	cat < viperjars >> temp.manifest
	cat < extjars >> temp.manifest
	jar cmf temp.manifest viper-gt.jar
	rm temp.manifest

viper-pe.jar: bin extjars viperjars
	touch temp.manifest
	echo Manifest-Version: 1.0 >> temp.manifest
	echo Main-Class: viper.comparison.ViperPE >> temp.manifest
	echo "Class-Path: . \\ " >> temp.manifest
	cat < viperjars >> temp.manifest
	cat < extjars >> temp.manifest
	jar cmf temp.manifest viper-pe.jar
	rm temp.manifest

viperjars:
	-mkdir lib
	-for x in api apploader chronicle jmpeg gt pe; do cp ../$$x/bin/$$x.jar lib; done
	ls lib/*.jar | perl -pe "s/^(.*)\$$/ \1 \\\\ /;" > viperjars

extjars:
	cd ../common/lib; ls *.jar *.zip | perl -pe "s/^(.*)\$$/ common\/lib\/\1 \\\\ /;" > ../../dist/extjars

bin:
	mkdir bin

clean:
	-rm *.jar *.jar.old *.tar.gz viperjars extjars
	-rm -r bin lib temp*

