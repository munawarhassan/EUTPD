


function tpd_build() {
  ${maven_cmd} clean package -Denv=docker ${maven_profiles} ${maven_args}
}

function tpd_up() {
  if [[ $pull -eq 1 ]]; then
    docker pull "postgres:${POSTGRES_TAG}"
    docker pull "docker.elastic.co/elasticsearch/elasticsearch:${ELASTICSEARCH_TAG}"
  fi
}



function tpd_down() {
  log ""
}

function tpd_clean() {
  stack_volume_rm "tpd"
}

function tpd_push() {
  if [[ -n "${REGISTRY_HOSTNAME:-}" ]]; then
    docker_tag_push_image "${REGISTRY_HOSTNAME}" "${TPD_FRONTEND_IMAGE_NAME}:${TPD_TAG}"
    docker_tag_push_image "${REGISTRY_HOSTNAME}" "${TPD_FRONTEND_IMAGE_NAME}:latest"

    docker_tag_push_image "${REGISTRY_HOSTNAME}" "${TPD_BACKEND_IMAGE_NAME}:${TPD_TAG}"
    docker_tag_push_image "${REGISTRY_HOSTNAME}" "${TPD_BACKEND_IMAGE_NAME}:latest"

    docker_tag_push_image "${REGISTRY_HOSTNAME}" "${TPD_RECEIVER_IMAGE_NAME}:${TPD_TAG}"
    docker_tag_push_image "${REGISTRY_HOSTNAME}" "${TPD_RECEIVER_IMAGE_NAME}:latest"
  else
    log "Regitry Required: You should reference a registry host"
  fi
}
