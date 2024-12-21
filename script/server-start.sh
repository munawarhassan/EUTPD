#!/bin/bash

dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

source ${dir}/setenv.sh

# default maven command
maven_cmd="mvn"
maven_profiles=""
maven_args=""
onlyStart=0
tpd_env="dev"

# store current arguments
args="$@"

for i in "$@"
do
case $i in
    -d|--docker)
    maven_cmd="${ROOT_PATH}/mvnd"
    shift
    ;;
    -w|--wrapper)
    maven_cmd="${ROOT_PATH}/mvnw"
    shift
    ;;
    -s|--skip-tests)
    maven_profiles="$( add_mvn_profile "${maven_profiles}" "skipTests" )"
    maven_args="${maven_args} -Dmaven.javadoc.skip=true"
    shift
    ;;
    -o|--only-start)
    onlyStart=1
    shift
    ;;
    *)
    # unknown option
    ;;
esac
done

# install in local repository

if [[ onlyStart -eq 0  ]]; then
  ${maven_cmd} clean install "$@" ${maven_profiles} ${maven_args}
fi
# run application in tomcat
${maven_cmd} org.codehaus.cargo:cargo-maven2-plugin:run -pl tpd-web -Denv=${tpd_env} ${maven_profiles} ${maven_args}
