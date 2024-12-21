FROM ubuntu

ARG DOMIBUS_VERSION
ARG DOCKERIZE_VERSION

ENV DOMIBUS_VERSION="${DOMIBUS_VERSION}" \
    DOCKERIZE_VERSION="${DOCKERIZE_VERSION}"

RUN set -x \
    && apt-get update \
	&& apt-get install -yq --no-install-recommends wget curl \
    && apt-get clean \
    && rm -r /var/lib/apt/lists/*

# install dockerize command line
RUN wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz --no-check-certificate\
    && tar -C /usr/local/bin -xzvf dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && rm dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz

WORKDIR /

COPY . /
RUN chmod +x /entrypoint.sh

ENTRYPOINT [ "/entrypoint.sh" ]
