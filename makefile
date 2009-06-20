
all: viper-cfg.sh viper.config
	. ./viper-cfg.sh; \
	for f in api pe chronicle jmpeg apploader gt ; \
	do \
	 cd $$f; \
	 make all; \
	 for j in bin/*.jar ; \
	 do \
	  CLASSPATH=`"/usr/bin/perl" "$${VIPER_ROOT}/scripts/addPath.pl" "CLASSPATH" "$${VIPER_ROOT}/$$f/$$j"` ; \
	 done ; \
	 cd .. ; \
	done

viper-cfg.sh viper.config:
	perl config.pl

clean:
	-rm viper.config viper-cfg.sh
	for f in api chronicle jmpeg apploader pe gt ; \
	 do cd $$f; make clean; cd .. ; \
	done

