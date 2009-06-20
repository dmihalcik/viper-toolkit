
#
# Please source before running graphics creation routines
#


if [ -z "${VIPER_ROOT}" ] || [ -z "${PERL_HOME}" ] || [ -z "${TKL_HOME}" ]
then
  echo "Some shell variables not set..."
  echo "Please source a viper-cfg.sh file"
  echo "required variables: VIPER_ROOT, PERL_HOME, TKL_HOME"
else
  set -a
  VIZ_HOME="${VIPER_ROOT}/viz"
  VIZ_BIN="$VIZ_HOME/bin"
  VIZ_WORKING="/tmp"
  PATH="${VIZ_BIN}:$PATH"
fi
