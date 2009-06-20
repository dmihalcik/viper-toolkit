
#
# Please source before running graphics creation routines
#


if [ -z "${VIPER_ROOT}" ] || [ -z "${PERL_HOME}" ]
then
  echo "Some variables not set..."
  echo "Please source a viper-cfg.sh file"
  echo "required variables: VIPER_ROOT, PERL_HOME"
else
  set -a
  SCRIPTS_HOME="${VIPER_ROOT}/scripts"
  PATH="${SCRIPTS_HOME}:$PATH"
fi
