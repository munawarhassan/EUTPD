#!/bin/bash

set -euo pipefail

# OS specific support.
cygwin=false;
darwin=false;
mingw=false
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  MINGW*) mingw=true;;
esac

dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

if $cygwin ; then
  [ -n "$dir" ] &&
    dir=`cygpath --unix "$dir"`
fi

set_profile() {
  if [ "$1" != "" ] && [ "$1" != "dev" ] && [ "$1" != "prod" ] && [ "$1" != "qa" ] && [ "$1" != "int" ]; then
    echo "wrong profile: '$1' (empty, dev, prod, qa and int only accepted)"
    exit 1
  fi
  if [[ "$1" == "dev"  ]]; then
    export TPD_PROFILES="development"
  fi
  if [[ "$1" == "qa"  ]]; then
    export TPD_PROFILES="qa"
  fi
  if [[ "$1" == "int"  ]]; then
    export TPD_PROFILES="integration"
  fi
  if [[ "$1" == "prod"  ]]; then
    export TPD_PROFILES="production"
  fi
}

export ROOT_PATH="${dir}/.."

export MAVEN_REPO_URL="https://rd-artifactory.app.pmi/artifactory/ThirdParty"
export MAVEN_REPO_ID=thirdparty

export REGISTRY_HOSTNAME="registry-192-168-1-175.traefik.me"

source ${dir}/extensions/shell.sh
source ${dir}/extensions/docker.sh
source ${dir}/extensions/maven.sh

initial_docker_options

# Create an environment variable with a username (you will use it for the HTTP Basic Auth for Traefik), for example
export USERNAME=admin
export ADMIN_USER=${USERNAME}
# Create an environment variable with the password, e.g.
export PASSWORD=changethis

# Use openssl to generate the "hashed" version of the password and store it in an environment variable
export HASHED_PASSWORD=$(openssl passwd -apr1 $PASSWORD)

export DOCKER_GATEWAY_HOST='$(get_gwbrigde_ip)'
# Get the Swarm node ID of this node and store it in an environment variable
export NODE_ID='$(docker info -f "{{.Swarm.NodeID}}")'
export SWARM_IP_DASHED='$(docker-machine ip $(docker-machine active) | tr . -)'
export DOCKER_GWBRIDGE='$(get_gwbrigde_ip)'

export TPD_TAG="$( get_project_version )"
export TPD_IMAGE_PREFIX="tpd/"
export TPD_BACKEND_IMAGE_NAME="${TPD_IMAGE_PREFIX}backend"
export TPD_BACKEND_IMAGE="${REGISTRY_HOSTNAME:+$REGISTRY_HOSTNAME/}${TPD_BACKEND_IMAGE_NAME:-tpd/tpd-backend}"

export TPD_FRONTEND_IMAGE_NAME="${TPD_IMAGE_PREFIX}frontend"
export TPD_FRONTEND_IMAGE="${REGISTRY_HOSTNAME:+$REGISTRY_HOSTNAME/}${TPD_FRONTEND_IMAGE_NAME:-tpd/tpd-frontend}"

export TPD_RECEIVER_IMAGE_NAME="${TPD_IMAGE_PREFIX}backend-receiver"
export TPD_RECEIVER_IMAGE="${REGISTRY_HOSTNAME:+$REGISTRY_HOSTNAME/}${TPD_RECEIVER_IMAGE_NAME:-tpd/backend-receiver}"

export DOMIBUS_IMAGE_NAME="domibus/domibus"
export DOMIBUS_IMAGE="${REGISTRY_HOSTNAME:+$REGISTRY_HOSTNAME/}${DOMIBUS_IMAGE_NAME:-domibus/domibus}"
export DOMIBUS_UPDATER_IMAGE_NAME="domibus/updater"
export DOMIBUS_UPDATER_IMAGE="${REGISTRY_HOSTNAME:+$REGISTRY_HOSTNAME/}${DOMIBUS_UPDATER_IMAGE_NAME:-domibus/updater}"
