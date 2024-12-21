#!/bin/bash

set -euo pipefail

dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
ROOT_PATH=${dir}/..


cert_path="${ROOT_PATH}/certs"
tpd_password='xkc]8Ae#cmP3NDbZz^p{xN9G8p`bNP5@](3__u$MY(>X+2dp[m[-'
store_type="pkcs12"

mkdir -p $cert_path || true
rm -r ${cert_path}/* || true


# generate base store
function gen_cert() {
  local keystoreAlias=$1
  local keystorePassword=$2
  local privateKeyPassword=$2
  echo "Generate certificate $keystoreAlias"
  # generate keypair
  keytool -genkeypair -dname  "C=BE,O=eDelivery,CN=${keystoreAlias}" -alias "${keystoreAlias}" \
    -keyalg RSA -keysize 2048 -keypass "${privateKeyPassword}" -validity  360 \
    -storetype JKS -keystore "${cert_path}/${keystoreAlias}.jks" -storepass "${keystorePassword}" -v
  # generate p12 store
  keytool -importkeystore -srckeystore "${cert_path}/${keystoreAlias}.jks" -destkeystore "${cert_path}/${keystoreAlias}.p12" -deststoretype pkcs12 \
    -srcstorepass "${keystorePassword}"  -deststorepass "${keystorePassword}"
  #extract certificate
  keytool -exportcert -alias "${keystoreAlias}" -file "${cert_path}/${keystoreAlias}.pem" -keystore "${cert_path}/${keystoreAlias}.jks" -storetype JKS \
    -storepass "${keystorePassword}" -rfc -v
}

# generate dev store used when use domibus test environment
function gen_dev_store() {
  local alias=$1
  local keystoreAlias=$2
  local keystorePassword=$3
  local keystoreFile=$4
  local newPassword="$tpd_password"

  echo "Generate dev keystore in $keystoreFile"
  cp "${cert_path}/${keystoreAlias}.jks" "${keystoreFile}"
  keytool -importcert -file "${cert_path}/${alias}.pem" -keystore "${keystoreFile}" -storepass "${keystorePassword}" -noprompt -alias "${alias}"

  keytool -storepasswd -new "$newPassword" -keystore  "${keystoreFile}" -storepass "$keystorePassword"
  keytool -keypasswd -alias "${keystoreAlias}" -keypass "$keystorePassword" -new "$newPassword" -keystore "${keystoreFile}" -storepass "$newPassword"
}

# generate sender store used in tpd-backend-core test
function gen_sender_store() {
  local alias=$1
  local keystoreAlias=$2
  local keystorePassword=$3
  local keystoreFile="${cert_path}/keystore-sender.jks"
  local newPassword="$tpd_password"

  echo "Generate sender keystore in $keystoreFile"
  cp "${cert_path}/${keystoreAlias}.jks" "${keystoreFile}"
  keytool -importcert -file "${cert_path}/${alias}.pem" -keystore "${keystoreFile}" -storepass "${keystorePassword}" -noprompt -alias "${alias}"
}

# generate receiver store used in tpd-backend-core test and docker receiver
function gen_receiver_store() {
  local alias=$1
  local keystoreAlias=$2
  local keystorePassword=$3
  local keystoreFile=$4

  echo "Generate receiver keystore in $keystoreFile"
  cp "${cert_path}/${keystoreAlias}.jks" "${keystoreFile}"
  keytool -importcert -file "${cert_path}/${alias}.pem" -keystore "${keystoreFile}" -storepass "${keystorePassword}" -noprompt -alias "${alias}"
}


function add_cert() {
  local alias=$1
  local keystorePassword=$2
  local keystoreFile=$3
  echo "add certificate $alias in truststore $keystoreFile"
  keytool -importcert -file "${cert_path}/${alias}.pem" -keystore "${keystoreFile}" \
    -storepass "${keystorePassword}" -noprompt -alias "${alias}" -deststoretype pkcs12
}


function add_key() {
  local alias=$1
  local keystorePassword=$2
  local keystoreFile=$3
   echo "add key $alias in keystore $keystoreFile"
  keytool -importkeystore -srckeystore "${cert_path}/${alias}.p12" -destkeystore "${keystoreFile}" -srcstoretype pkcs12 -alias "${alias}" \
  -srcstorepass "${keystorePassword}"  -deststorepass "${keystorePassword}" -deststoretype pkcs12
}

certificate="EUCEG_EC"
keypair="ACC-EUCEG-99962-AS4"
password="test123"

gen_cert "${certificate}" "${password}"
gen_cert "${keypair}" "${password}"

# add certificate in domibus gateway truststore
truststoreFile="${cert_path}/tpd_gateway_truststore.p12"
add_cert "${certificate}" "${password}" "${truststoreFile}"
add_cert "${keypair}" "${password}" "${truststoreFile}"
cp -f "${truststoreFile}" "$ROOT_PATH/docker/domibus/config/domibus/truststore/tpd_gateway_truststore.p12"

# add keypair in domibus gateway keystore
keystoreFile="${cert_path}/tpd_gateway_keystore.p12"
add_key "${certificate}" "${password}" "${keystoreFile}"
add_key "${keypair}" "${password}" "${keystoreFile}"
cp -f "${keystoreFile}" "$ROOT_PATH/docker/domibus/config/domibus/keystore/tpd_gateway_keystore.p12"


devStore="${cert_path}/keystore-dev.jks"
gen_dev_store "${certificate}" "${keypair}" "${password}"  "${devStore}"
cp -f "${devStore}" "$ROOT_PATH/tpd-web/src/main/resources/certificates/keystore-dev.jks"

senderKeystore="${cert_path}/keystore-sender.jks"
gen_sender_store "${certificate}" "${keypair}" "${password}" "${senderKeystore}"

receiverKeystore="${cert_path}/keystore-receiver.jks"
gen_receiver_store "${keypair}" "${certificate}" "${password}" "${receiverKeystore}"
cp -f "${receiverKeystore}" "$ROOT_PATH/docker/domibus/config/receiver/keystore-receiver.jks"
