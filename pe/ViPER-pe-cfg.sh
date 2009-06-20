
if [ -z "${VIPER_ROOT}" ] || [ -z "${JAVA}" ]
then
  echo "Some Environment variables not set..."
  echo "Please source a viper-cfg.sh file"
  echo "required variables: VIPER_ROOT, JAVA"
else
  set -a
  GTFC_HOME="$VIPER_ROOT/pe"
  GTFC_DATA="$GTFC_HOME/data"
  GTFC_SRC="$GTFC_HOME/src"
  GTFC_BIN="$GTFC_HOME/bin"
  PATH=`"${PERL_HOME}/perl" "${VIPER_ROOT}/scripts/addPath.pl" "PATH" "${GTFC_BIN}"`
  CLASSPATH="${CLASSPATH}:$VIPER_ROOT/api/bin/viper-api.jar"
fi
