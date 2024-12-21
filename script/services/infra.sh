

function infra_build() {
  log ""
}

function infra_up() {
  if [[ $pull -eq 1 ]]; then
    docker pull "traefik:v2.4"
    docker pull "registry:2.7.1"
    docker pull "swarmpit/swarmpit:latest"
    docker pull "swarmpit/agent:latest"
    docker pull "couchdb:2.3.0"
    docker pull "influxdb:1.7"
  fi
}

function infra_down() {
  log ""
}

function infra_clean() {
  stack_volume_rm "infra"  
}

function infra_push() {
  log ""
}
