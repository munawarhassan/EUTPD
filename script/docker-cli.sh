#!/bin/bash

set -euo pipefail


dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
ROOT_PATH=${dir}/..

source ${dir}/setenv.sh

services_list=`ls ${dir}/services/*`
for f in $services_list
do
   source $f
done


export CONFIGURATION_PATH=""

# all available services
declare -a all_services=("tpd" "domibus" "c2c3" "monitoring" "infra" "sdlc")
# all services to use
services=()
# name of discovery network
discovery_network="discovery"
# name of net network
net_network="net"
# default name of machine to use
default_context="neptune"
active_context=""
# default maven command to use
maven_cmd="mvn"
# contains all profiles to use
maven_profiles=""
# maven arguments
maven_args=""
# the current action
action=""
# activate pulling of image before 'up' action
pull=0
# activate provisioning of config before 'up' action
provision=0


function get_usage() {
  log ""
  log "Usage:  docker-cli [OPTIONS] COMMAND [SERVICES|all,DEFAULT=tpd]"
  log "Commands:"
  log "    build      Build an image"
  log "    push       Publish on registry or transfer on docker machine"
  log "    clean      Delete all volumes"
  log "    up         Deploy TPD stack or update an existing stack"
  log "    down       Remove TPD stack"
  log "    restart    Down & up stack"
  log "    provision  Provision config data"
  log "    rotate     Rotate all Service certificates"
}

function create_network() {
  # Create a network that will be shared with Traefik and the containers that should be accessible from the outside, with
  if [[ -z "$(network_exists ${discovery_network})" ]]; then
    doing "Creating discovery network"
    docker network create -d overlay --attachable ${discovery_network}
  fi
  if [[ -z "$(network_exists $net_network)" ]]; then
    doing "Creating $net_network network"
    docker network create -d overlay --attachable $net_network
  fi
  for server in $(docker node ls -f "role=manager" --format {{.Hostname}} | tr '\r\n' ' ')
  do
    # Create a tag in this node, so that Traefik is always deployed to the same node and uses the same volume
  docker node update --label-add manager.discovery=true $server >/dev/null
  # same case for global monitoring
  docker node update --label-add manger.monitoring=true $server >/dev/null
  done
  # set all worker as rack
  for server in $(docker node ls -f "role=worker" --format {{.Hostname}} | tr '\r\n' ' ')
  do
    docker node update --label-add rack $server >/dev/null
  done
}



function apply_provision() {
  set_context
  local active_remote="$(docker-machine active)"
  local local_provisioning_path="${ROOT_PATH}/docker/provisioning"
  local unmount=0;

  log "provision parameter: $@"
  for i in "$@"
  do
  case $i in
      -u|--unmount)
      unmount=1
      shift
      ;;
  esac
  done

  if [[ -n "${active_remote}" ]]; then

    # search the home remote path
    remote_path="$(docker-machine ssh ${active_remote} pwd)/provisioning"

    export CONFIGURATION_PATH="${remote_path}"

    if [[ ! -d "${local_provisioning_path}" ]]; then
      # create temporary provisioning folder
      doing "Creating temporary provisioning folder: ${local_provisioning_path}"
      mkdir -p "${local_provisioning_path}"
    fi
    if [[ $unmount -eq 0 ]]; then
      # create directory on remote
      doing "Creating temporary remote provisioning folder"
      docker-machine ssh ${active_remote} "[[ ! -d provisioning ]] &&  mkdir -p provisioning || true"

      # mount sshfs share folder
      doing "Mounting sshfs share folder"
      docker-machine mount ${active_remote}:${remote_path} "${local_provisioning_path}" || true

      # remove temporary provisioning folder
      doing "Clean directory ${local_provisioning_path}"
      rm -rf "${local_provisioning_path}"/* || true
      # copy all configs in share folder
      doing "Copying ${ROOT_PATH}/docker/etc/ folder to provisioning volume"
      cp -af ${ROOT_PATH}/docker/etc/* $local_provisioning_path
    else
      # unmount share folder
      # macfuse use umount command instead
      doing "Umount ${local_provisioning_path}"
      if  ! cmd_exists fusermount; then
        # try using umount
        umount "${local_provisioning_path}" || true
      else
        docker-machine mount -u "${local_provisioning_path}"
      fi
      docker-machine ssh ${active_remote} "[[ -d provisioning ]] &&  rm -r provisioning || true"
      # remove temporary provisioning folder
      rm -r "${local_provisioning_path}" || true
    fi
  fi
  unset_context
}

function docker_build() {
  source ./docker/.env
  for selected_service in "${services[@]}"
  do
    eval "${selected_service}_build"
  done
}

# push tpd image to registry if exists, otherwise try transfer to active or default host machine
function docker_push() {
  qpushd docker
  source .env
  for selected_service in "${services[@]}"
  do
    eval "${selected_service}_push"
  done
  qpopd
}



function docker_up() {
  set_context
  local compose_file
  local active_remote="$(docker-machine active)"

  qpushd docker
  source .env

  # create discovery attachable network
  create_network

  if [[ -n "${active_remote}" ]]; then
    export CONFIGURATION_PATH="$(docker-machine ssh ${active_remote} pwd)/provisioning"
  else
    export CONFIGURATION_PATH="${ROOT_PATH}/docker/etc"
  fi
  doing "CONFIGURATION_PATH: ${CONFIGURATION_PATH}"
  # if [ "$(volume_exists provisioning)" = "" ]; then
  #     echo "- Create Volume : provisioning"
  #     docker volume create --name provisioning
  # fi

  # start infra if no running
  if [[ -z "$(stack_exists infra)" ]]; then
    services+=("infra")
  fi

  for selected_service in "${services[@]}"
  do
    eval "${selected_service}_up"
    compose_file="docker-compose.${selected_service}.yml"

    if [[ -f $compose_file ]]; then
      doing " Convert '${selected_service}' compose file and deploy"
      # add compose version
      # substition var env
      # remove first line "name: docker"
      # remove double quotes port.published
      ( grep version: "${compose_file}"; docker-compose -f "${compose_file}" config | sed '1d' ) | sed '/published:/s/\"//g' | docker stack deploy --with-registry-auth -c - ${selected_service}
    else
      printf "${red_color}WARNING${nc_color}: the file ${compose_file} doesn't exist$"
    fi

  done

  qpopd
  unset_context
}

function docker_down() {
  set_context
  for selected_service in "${services[@]}"
  do
    doing "Stop stack ${selected_service} and remove containers, networks"
    eval "${selected_service}_down"
    # TODO: add check to verify tpd can shutdown (started or failed but not starting)
    stack_down "${selected_service[@]}"
  done
  unset_context
}

function docker_clean() {
  set_context
  for selected_service in "${services[@]}"
  do
    if [ -n "$(stack_exists ${selected_service})" ]; then
        echo "Shudown the service ${selected_service}"
        docker_down
    fi
    eval "${selected_service}_clean"
  done
  unset_context
}

function docker_test() {
  echo "test"
}

function set_context() {
  if [[ -n "${active_context}" ]]; then
    doing "Set up the environment for the Docker client to ${active_context}"
    eval $(docker-machine env ${active_context})
  fi
}

function unset_context() {
  if [[ -n "${active_context}" ]]; then
    doing "Set up the environment for the Docker client to default"
    eval "$(docker-machine env --unset)"
  fi
}

function rotate_cert() {
  set_context
  for server in $(docker node ls --format {{.Hostname}} | tr '\r\n' ' ')
  do
    doing "Rotate Registry certificate on ${server}"
    cmd="docker-machine ssh ${server}"
    ${cmd} wget https://traefik.me/cert.pem
    ${cmd} sudo mv cert.pem /etc/docker/certs.d/registry-192-168-1-175.traefik.me/ca.crt
    ${cmd} sudo systemctl restart docker
  done
}

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
    -p=*|--profiles=*)
    maven_profiles="$( add_mvn_profile "${maven_profiles}" "${i#*=}" )"
    shift
    ;;
    -c=*|--context=*)
    active_context="${i#*=}"
    shift
    ;;
    -t=*|--tag=*)
    TPD_TAG="${i#*=}"
    shift
    ;;
    -s|--skip-tests)
    maven_profiles="$( add_mvn_profile "${maven_profiles}" "skipTests" )"
    maven_args="${maven_args} -Dmaven.javadoc.skip=true"
    shift
    ;;
    -a=*|--args=*)
    maven_args="${maven_args} ${i#*=}"
    shift
    ;;
    --pull)
    pull=1
    shift
    ;;
    build|push|up|clean|down|restart|provision|rotate|test)
    action="${i}"
    shift
    ;;
    *)
    ## action should be first
    if [[ -n "${action}" && ${i:0:1} != "-" ]]; then
      services+=("${i}")
    fi
    shift
    ;;
esac
done

if [[ -z "${action}" ]]; then
  get_usage
  exit 1
fi
# default service
if [[ ${#services[@]} -eq 0 ]]; then
  services+=("tpd")
fi

# all services
if [[ ${#services[@]} -eq 1 && ${services[0]} = "all" ]]; then
  services=(${all_services[@]})
fi

if [[ -z "${active_context}" ]]; then
  active_context="${default_context}"
fi

log "--------------------------------------------------------------"
log "Action              : ${action}"
log "maven_profiles      : ${maven_profiles}"
log "maven_args          : ${maven_args}"
log "services            : ${services[@]}"
log ""
log "TPD version         : ${TPD_TAG}"
log "Frontend image name : ${TPD_FRONTEND_IMAGE}:${TPD_TAG:-latest}"
log "Backend image name  : ${TPD_BACKEND_IMAGE}:${TPD_TAG:-latest}"
log "--------------------------------------------------------------"
log "REGISTRY            : ${REGISTRY_HOSTNAME:-<No>}"
log "context             : ${active_context:-<No>}"
log ""




case $action in
    build)
    docker_build
    ;;
    push)
    docker_push
    ;;
    up)
    docker_up
    ;;
    down)
    docker_down
    ;;
    restart)
    docker_down
    docker_up
    ;;
    clean)
    docker_clean
    ;;
    provision)
    apply_provision $args
    ;;
    rotate)
    rotate_cert
    ;;
    test)
    docker_test
    ;;
    *)
    get_usage
    exit 1
    ;;
esac


