#!/bin/bash

dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

export DOMAIN="neptune.local"
export SHORT_NAME="registry"

# mkcert example
# mkcert -cert-file certs/local-cert.pem -key-file certs/local-key.pem "docker.localhost" "*.docker.localhost" "domain.local" "*.domain.local"

cat > /tmp/${SHORT_NAME}_answer.txt << EOF
[req]
default_bits = 4096
prompt = no
default_md = sha256
x509_extensions = req_ext
req_extensions = req_ext
distinguished_name = dn

[ dn ]
C=US
ST=New York
L=New York
O=MyOrg
OU=MyOrgUnit
emailAddress=me@working.me
CN = ${SHORT_NAME}

[ req_ext ]
subjectAltName = @alt_names

[ alt_names ]
DNS.1 = ${SHORT_NAME}
DNS.2 = ${SHORT_NAME}.${DOMAIN}
DNS.3 = *.${DOMAIN}
EOF

cat > /tmp/csr_ca.txt << EOF
[req]
default_bits = 4096
prompt = no
default_md = sha256
distinguished_name = dn
x509_extensions = usr_cert

[ dn ]
C=US
ST=New York
L=New York
O=MyOrg
OU=MyOU
emailAddress=me@working.me
CN = server.example.com

[ usr_cert ]
basicConstraints=CA:TRUE
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid,issuer
EOF

#Generate the Key
openssl genrsa -out ${dir}/certs/ca.key 4096

# Generate the CA
openssl req -new -x509 -key ${dir}/certs/ca.key -days 730 -out ${dir}/certs/ca.crt -config <( cat /tmp/csr_ca.txt )

# Generate Server Key
openssl genrsa -out ${dir}/certs/domain.key 4096

# Generate Server CSR
openssl req -new -key ${dir}/certs/domain.key -out ${dir}/certs/domain.csr -config <( cat /tmp/${SHORT_NAME}_answer.txt )

# Sign the CSR
openssl x509 -req -in ${dir}/certs/domain.csr -CA ${dir}/certs/ca.crt -CAkey ${dir}/certs/ca.key -CAcreateserial -out ${dir}/certs/domain.crt -days 730 -extensions 'req_ext' -extfile <(cat /tmp/${SHORT_NAME}_answer.txt)


rm /tmp/csr_ca.txt
rm /tmp/${SHORT_NAME}_answer.txt

cat ${dir}/certs/domain.crt ${dir}/certs/ca.crt > ${dir}/certs/fullchain.domain.crt
