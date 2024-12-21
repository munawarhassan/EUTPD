# syntax=docker/dockerfile:1
FROM node:18 as builder

WORKDIR /app

COPY . .

RUN npm install
RUN npm run server:build:prod && \
    npm run build:prod

FROM node:18-alpine as stage

ENV PORT=3000 \
    HOST="0.0.0.0" \
    MIDDLEWARE_TARGET="http://localhost:8080" \
    CHECK_PUBLIC_HOSTNAME="false" \
    PUBLIC_HOSTNAME="" \
    NPM_CONFIG_LOGLEVEL="error" \
    NODE_ENV="production" \
    HOME_DIRECTORY="/apps" \
    SERVER_DIRECTORY="/apps/server" \
    DIST_DIRECTORY="/apps/tpd" \
    DUMB_INIT_VERSION="1.2.5"

ADD https://github.com/Yelp/dumb-init/releases/download/v${DUMB_INIT_VERSION}/dumb-init_${DUMB_INIT_VERSION}_x86_64 /usr/local/bin/dumb-init
RUN chmod +x /usr/local/bin/dumb-init

WORKDIR "${HOME_DIRECTORY}"

RUN addgroup --system server && \
          adduser --system -G server server

COPY --from=builder /app/dist/packages/server ./server
COPY --from=builder /app/dist/apps/tpd ./tpd


RUN chown -R server:server .

EXPOSE 3000

ENTRYPOINT ["dumb-init", "--"]
CMD [ "sh", "-c", "node server -o ${DIST_DIRECTORY}"]
