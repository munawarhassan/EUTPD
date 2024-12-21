

function c2c3_build() {
  log ""
}

function c2c3_up() {
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



function c2c3_down() {
  log ""
}

function c2c3_clean() {
  stack_volume_rm "c2c3"
}

function c2c3_push() {
  log ""
}
