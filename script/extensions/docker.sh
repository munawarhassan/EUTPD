#!/bin/bash

set -euo pipefail


function stack_down {
  local service_name=$1
  docker stack rm ${service_name}
  local is_first=1
  while [[ -n "$(docker stack ps ${service_name} -q 2>/dev/null | tr -d '[:space:]')" ]]; do
    if [[ is_first -eq 1 ]]; then
      echo "Please wait..."
      printf "${blue_bg}Shutting down"
      is_first=0
    fi
    printf "."
    sleep 5
  done
  log "${nc_color}"
  doing "Stack ${service_name} is shutdown"
}

function stack_volume_rm() {
  local stack=$1
  local active_serve="$(docker-machine active)"
  log "${blue_color}>>>${nc_color} Remove all volumes of ${stack} service"
  for server in $(docker-machine ls -f {{.Name}} | tr '\r\n' ' ')
  do
    # activate the server
    eval "$(docker-machine env ${server})"
    local volumes=$(docker volume ls --format "{{.Name}}" -f "label=com.docker.stack.namespace=$stack")
    if [[ -n $volumes ]]; then
      docker volume rm $volumes
    fi
  done
  # restore the old active server
  if [[ -n "${active_serve}" ]]; then
      eval "$(docker-machine env $active_serve)"
  fi

}

function stack_exists {
  local service_name=$1
  echo "$(docker stack ls --format "{{.Name}}" | grep ${service_name} | tr -d '[:space:]')"
}

function container_exists {
    local name=$1
    echo "$(docker ps -a -f name=^/${name}$ --format "{{.Names}}" | tr -d '[:space:]')"
}



function container_running {
    local name=$1
    echo "$(docker container ls -f name=^/${name}$ --format "{{.Names}}" | tr -d '[:space:]')"
}

function image_exists {
    local image_name=$1
    echo "$(docker image ls --format "{{.Repository}}" | grep ${image_name} | tr -d '[:space:]')"
}

function image_remove {
  if [[ -n "$( docker images -q  $1 )" ]]; then
    docker ${DOCKER_HOST:-} rmi $1
  fi
}

function volume_exists {
    local volume_name=$1
    echo "$(docker volume ls -q -f name=${volume_name} | tr -d '[:space:]')"
}

function volume_remove {
  if [[ -n "$( volume_exists $1 )" ]]; then
    docker volume rm $1
  fi
}

function network_exists {
    local network_name=$1
    echo "$(docker network ls -q -f name=${network_name} | tr -d '[:space:]')"
}

function get_gwbrigde_ip {
  echo "$(docker network inspect docker_gwbridge -f '{{range .IPAM.Config}}{{.Gateway}}{{end}}')"
}


function initial_docker_options {
  local dockerOptions=""
  # use remote access if docker host inquire
  if [[ ! -z "${DOCKER_HOST:-}" ]]; then
      dockerOptions="-h ${DOCKER_HOST}"
  fi
  export DOCKER_OPTIONS=${dockerOptions}
}

function docker_tag_push_image() {
  local registry=$1
  local image=$2

  doing "Tag image ${image} to ${registry}/${image}"
  docker tag "${image}" "${registry}/${image}"
  doing "Push ${registry}/${image}"
  docker push "${registry}/${image}"
}


function image_to_server() {
  local image=$1
  local server=$2
  local active="$(docker-machine active)"

  doing "Saving image ${image} from server $active"
  # create package from image
  docker save "${image}" | gzip >  /tmp/backup.tar.gz
  # activate the server
  eval "$(docker-machine env ${server})"

  doing "Loading image ${image} to server $server"
  # load package on server
  docker load < /tmp/backup.tar.gz
  # unactivate the server
  eval "$(docker-machine env -u)"
  # remove backup package
  rm /tmp/backup.tar.gz
  # restore the old active server
  if [[ -n "${active}" ]]; then
      eval "$(docker-machine env $active)"
  fi
}



