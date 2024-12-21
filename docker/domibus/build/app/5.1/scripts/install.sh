#!/bin/bash


function load_prerequisite() {
  pushd ${DOMIBUS_INSTALL}

  # load sample configuration
  wget ${EDELIVERY_REPO}/eeu/domibus/domibus-msh-distribution/$DOMIBUS_VERSION/domibus-msh-distribution-${DOMIBUS_VERSION}-sample-configuration-and-testing.zip --no-check-certificate
  # load ws plugin
  wget ${EDELIVERY_REPO}/eu/domibus/domibus-msh-distribution/$DOMIBUS_VERSION/domibus-msh-distribution-${DOMIBUS_VERSION}-default-ws-plugin.zip --no-check-certificate
  # load jms plugin
  wget ${EDELIVERY_REPO}/eu/domibus/domibus-msh-distribution/$DOMIBUS_VERSION/domibus-msh-distribution-${DOMIBUS_VERSION}-default-jms-plugin.zip --no-check-certificate
  # load fs plugin
  wget ${EDELIVERY_REPO}/eu/domibus/domibus-msh-distribution/$DOMIBUS_VERSION/domibus-msh-distribution-${DOMIBUS_VERSION}-default-fs-plugin.zip --no-check-certificate
  popd
}


load_prerequisite
