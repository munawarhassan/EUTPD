#!/bin/bash

set -euo pipefail
dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
set -x


source ${dir}/.env

DOMIBUS_UPDATER_IMAGE_NAME="domibus/updater"
DOMIBUS_UPDATER_IMAGE="${REGISTRY_HOSTNAME:+$REGISTRY_HOSTNAME/}${DOMIBUS_UPDATER_IMAGE_NAME:-domibus/updater}"
DOMIBUS_IMAGE_NAME="domibus/domibus"
DOMIBUS_IMAGE="${REGISTRY_HOSTNAME:+$REGISTRY_HOSTNAME/}${DOMIBUS_IMAGE_NAME:-domibus/domibus}"


docker build \
  --rm \
  -t "${DOMIBUS_IMAGE_NAME}:${DOMIBUS_MAJOR_VERSION}" \
  -t "${DOMIBUS_IMAGE_NAME}:${DOMIBUS_MAJOR_MINOR_VERSION}" \
  -t "${DOMIBUS_IMAGE_NAME}:${DOMIBUS_VERSION}" \
  -t "${DOMIBUS_IMAGE_NAME}:latest" \
  --build-arg DOMIBUS_VERSION="${DOMIBUS_VERSION}"\
    --build-arg DOMIBUS_TIMESTAMP="${DOMIBUS_TIMESTAMP}"\
  --build-arg MYSQL_DRIVER_VERSION="${DOMIBUS_MYSQL_DRIVER_VERSION}"\
  --build-arg LIQUIBASE_VERSION="${DOMIBUS_LIQUIBASE_VERSION}"\
  --build-arg DOCKERIZE_VERSION="${DOCKERIZE_VERSION}"\
  "${dir}/build/app/${DOMIBUS_MAJOR_MINOR_VERSION}"

docker build \
  --rm \
  -t "${DOMIBUS_UPDATER_IMAGE_NAME}:${DOMIBUS_MAJOR_VERSION}" \
  -t "${DOMIBUS_IMAGE_NAME}:${DOMIBUS_MAJOR_MINOR_VERSION}" \
  -t "${DOMIBUS_UPDATER_IMAGE_NAME}:${DOMIBUS_VERSION}" \
  -t "${DOMIBUS_UPDATER_IMAGE_NAME}:latest" \
  --build-arg DOMIBUS_VERSION="${DOMIBUS_VERSION}"\
  --build-arg DOCKERIZE_VERSION="${DOCKERIZE_VERSION}"\
  ${dir}/build/updater
