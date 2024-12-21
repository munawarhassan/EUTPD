


function domibus_build() {
  docker build \
    --rm \
    -t "${DOMIBUS_IMAGE_NAME}:${DOMIBUS_MAJOR_VERSION}" \
    -t "${DOMIBUS_IMAGE_NAME}:${DOMIBUS_VERSION}" \
    -t "${DOMIBUS_IMAGE_NAME}:latest" \
    --build-arg DOMIBUS_VERSION="${DOMIBUS_VERSION}"\
    --build-arg DOMIBUS_TIMESTAMP="${DOMIBUS_TIMESTAMP}"\
    --build-arg MYSQL_DRIVER_VERSION="${DOMIBUS_MYSQL_DRIVER_VERSION}"\
    --build-arg LIQUIBASE_VERSION="${DOMIBUS_LIQUIBASE_VERSION}"\
    --build-arg DOCKERIZE_VERSION="${DOCKERIZE_VERSION}"\
    "${ROOT_PATH}/docker/domibus/build/${DOMIBUS_VERSION}"

  docker build \
    --rm \
    -t "${DOMIBUS_UPDATER_IMAGE_NAME}:${DOMIBUS_MAJOR_VERSION}" \
    -t "${DOMIBUS_UPDATER_IMAGE_NAME}:${DOMIBUS_VERSION}" \
    -t "${DOMIBUS_UPDATER_IMAGE_NAME}:latest" \
    --build-arg DOMIBUS_VERSION="${DOMIBUS_VERSION}"\
    --build-arg DOCKERIZE_VERSION="${DOCKERIZE_VERSION}"\
    ${ROOT_PATH}/docker/domibus/updater
}

function domibus_up() {
  if [[ $pull -eq 1 ]]; then
    doing "Pull latest Domibus version"
    # TODO need to pull on each nodes
    # docker pull "mysql:${DOMIBUS_MYSQL_VERSION}"
    # docker pull "${DOMIBUS_IMAGE}:${DOMIBUS_VERSION}"
    # docker pull "${DOMIBUS_IMAGE}:${DOMIBUS_MAJOR_VERSION}"
    # docker pull "${DOMIBUS_UPDATER_IMAGE}:${DOMIBUS_VERSION}"
    # docker pull "${DOMIBUS_UPDATER_IMAGE}:${DOMIBUS_MAJOR_VERSION}"
  fi
}

function domibus_down() {
  log ""
}

function domibus_clean() {
  stack_volume_rm "domibus"
}


function domibus_push() {
  if [[ -n "${REGISTRY_HOSTNAME:-}" ]]; then
    docker_tag_push_image "${REGISTRY_HOSTNAME}" "${DOMIBUS_IMAGE_NAME}:${DOMIBUS_VERSION}"
    docker_tag_push_image "${REGISTRY_HOSTNAME}" "${DOMIBUS_IMAGE_NAME}:${DOMIBUS_MAJOR_VERSION}"
    docker_tag_push_image "${REGISTRY_HOSTNAME}" "${DOMIBUS_IMAGE_NAME}:latest"
    docker_tag_push_image "${REGISTRY_HOSTNAME}" "${DOMIBUS_UPDATER_IMAGE_NAME}:${DOMIBUS_VERSION}"
    docker_tag_push_image "${REGISTRY_HOSTNAME}" "${DOMIBUS_UPDATER_IMAGE_NAME}:${DOMIBUS_MAJOR_VERSION}"
    docker_tag_push_image "${REGISTRY_HOSTNAME}" "${DOMIBUS_UPDATER_IMAGE_NAME}:latest"
  else
    log "Regitry Required: You should reference a registry host"
  fi
}
