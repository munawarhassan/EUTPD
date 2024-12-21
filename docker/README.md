
# Deployment

## Deploy on Swarm

```bash
docker-compose -f docker-compose.yml config | docker stack deploy -c - tpd
```

## security check

```bash
$ docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -v caches:/root/.cache/ aquasec/trivy &lt;**IMAGE**&gt;
```

## "docker pull" certificate signed by unknown authority

c'est le cert de traefik.me https://traefik.me/cert.pem

```bash
$ openssl s_client -showcerts -connect registry-192-168-1-175.traefik.me:443 < /dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > /etc/docker/certs.d/registry-192-168-1-175.traefik.me/ca.crt
$ ex +’/BEGIN CERTIFICATE/,/END CERTIFICATE/p’ <(echo | openssl s_client -showcerts -connect registry-192-168-1-175.traefik.me:443) -scq > /etc/docker/certs.d/registry-192-168-1-175.traefik.me/docker_registry.crt
```

```bash
$ wget https://traefik.me/cert.pem
$ sudo mv cert.pem /etc/docker/certs.d/registry-192-168-1-175.traefik.me/ca.crt
$ sudo systemctl restart docker
```


MacOS

```
$ openssl s_client -showcerts -connect registry-192-168-1-175.traefik.me:443 < /dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > cert.pem
$ security add-trusted-cert -d -r trustRoot -k ~/Library/Keychains/login.keychain ./cert.pem
```