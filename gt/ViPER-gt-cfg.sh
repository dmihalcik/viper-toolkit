#
# ViPER Configuration
#

#
# Note: location of data file and config files are changed in 
#       $VIPER_PROP/ViPER_GUI.properties
#

#
# Install directory for ViPER
#
set -a
VIPER_HOME="${VIPER_ROOT}/gt"
CLASSPATH="${CLASSPATH}:$VIPER_ROOT/pe/bin/viper-pe.jar:$VIPER_ROOT/api/bin/viper-api.jar"
#:$VIPER_ROOT/apploader/bin/viper-apploader.jar
#:$VIPER_ROOT/chronicle/bin/viper-chronicle.jar
#:$VIPER_ROOT/jmpeg/bin/viper-jmpeg.jar

#
# Set locations of the properties file - which controls where to
# obtain the data, gtf and where to place dumps among other things 
#
VIPER_PROP="$VIPER_HOME/CONFIG"
VIPER_CFG_PREFS="$VIPER_HOME/CONFIG/gtc-config.n3"
VIPER_DATA_PREFS="$VIPER_HOME/CONFIG/gt-config.n3"

VIPER_BIN="$VIPER_HOME/bin"

#
# Add ViPER to the current path
#
PATH=`${PERL_HOME}/perl "${VIPER_ROOT}/scripts/addPath.pl" "PATH" "${VIPER_BIN}"`


