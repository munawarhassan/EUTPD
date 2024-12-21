import http, { IncomingHttpHeaders } from 'http';
import https from 'https';
import path from 'path';
import url from 'url';
import compression from 'compression';
import express, { NextFunction, Request, Response } from 'express';
import { Express, RequestHandlerParams } from 'express-serve-static-core';
import fs from 'graceful-fs';
import internalIp from 'internal-ip';
import ipaddr from 'ipaddr.js';
import killable from 'killable';
import spdy from 'spdy';
import WebSocket from 'ws';
import middlelware from './middleware';
import { findPort, getCertificate, normalizeOptions } from './utils';
import { WebsocketServer } from './websocket-server';
import { Configuration } from 'webpack-dev-server';
import { createProxyMiddleware, RequestHandler } from 'http-proxy-middleware';

interface Killable {
    kill: (cb) => void;
}

type HttpServer = http.Server | https.Server;

export class Server {
    public wsHeartbeatInterval: number;
    public options: Configuration;
    public server: HttpServer;
    public logger: Console;

    private sockets: any[];
    private websocketProxies: RequestHandler[];
    private socketServer: WebsocketServer;
    private app: Express;
    private middleware: any;

    private hostname: string;
    private port: number;

    constructor(options: Configuration = {}) {
        this.logger = console;
        this.options = options;
        this.sockets = [];
        // Keep track of websocket proxies for external websocket upgrade.
        this.websocketProxies = [];
        // this value of ws can be overwritten for tests
        this.wsHeartbeatInterval = 30000;

        normalizeOptions(this.options);
        this.setupApp();
        // if (!this.options.disableHostCheck) {
        //     this.setupCheckHostRoute();
        // }
        this.setupMiddleware();

        this.setupFeatures();
        this.setupHttps();
        this.createServer();

        killable(this.server);

        // Proxy WebSocket without the initial http request
        // https://github.com/chimurai/http-proxy-middleware#external-websocket-upgrade
        // eslint-disable-next-line func-names
        this.websocketProxies.forEach(function (wsProxy) {
            this.server.on('upgrade', wsProxy.upgrade);
        }, this);
    }

    public use(...handlers: RequestHandlerParams[]) {
        // eslint-disable-next-line prefer-spread
        this.app.use.apply(this.app, handlers);
    }

    public listen(port: number, hostname: string, fn?: (server: HttpServer, error: any) => void) {
        if (hostname === 'local-ip') {
            this.hostname = internalIp.v4.sync() || internalIp.v6.sync() || '0.0.0.0';
        } else if (hostname === 'local-ipv4') {
            this.hostname = internalIp.v4.sync() || '0.0.0.0';
        } else if (hostname === 'local-ipv6') {
            this.hostname = internalIp.v6.sync() || '::';
        } else {
            this.hostname = hostname;
        }

        if (typeof port !== 'undefined' && port !== this.options.port) {
            this.logger.warn('The port specified in options and the port passed as an argument is different.');
        }

        return (
            findPort(port || this.options.port)
                // eslint-disable-next-line no-shadow
                .then((aPort) => {
                    this.port = aPort;
                    return this.server.listen(aPort, this.hostname, null, (/* error*/) => {
                        const error = null;
                        if (this.options.hot || this.options.liveReload) {
                            this.createSocketServer();
                        }

                        if (fn) {
                            fn.call(this.server, error);
                        }
                    });
                })
                .catch((error) => {
                    if (fn) {
                        fn.call(this.server, error);
                    }
                })
        );
    }

    public close(cb: () => void) {
        this.sockets.forEach((socket) => {
            this.socketServer.close(socket);
        });

        this.sockets = [];

        const killed = this.server as unknown as Killable;
        killed.kill(() => {
            this.middleware.close(cb);
        });
    }

    public invalidate(callback) {
        if (this.middleware) {
            this.middleware.invalidate(callback);
        }
    }

    private setupApp() {
        // Init express server
        this.app = express();
    }

    private setupCheckHostRoute() {
        this.app.all('*', (req, res, next) => {
            if (this.checkHost(req.headers)) {
                return next();
            }

            res.send('Invalid Host header');
        });
    }

    private setupMiddleware() {
        // middleware for serving webpack bundle
        this.middleware = middlelware(this.options.devMiddleware);
    }

    private setupCompressFeature() {
        this.app.use(compression());
    }

    setupProxyFeature() {
        /**
         * Assume a proxy configuration specified as:
         * proxy: {
         *   'context': { options }
         * }
         * OR
         * proxy: {
         *   'context': 'target'
         * }
         */
        if (!Array.isArray(this.options.proxy)) {
            if (Object.prototype.hasOwnProperty.call(this.options.proxy, 'target')) {
                this.options.proxy = [this.options.proxy];
            } else {
                this.options.proxy = Object.keys(this.options.proxy).map((context) => {
                    let proxyOptions;
                    // For backwards compatibility reasons.
                    const correctedContext = context.replace(/^\*$/, '**').replace(/\/\*$/, '');

                    if (typeof this.options.proxy[context] === 'string') {
                        proxyOptions = {
                            context: correctedContext,
                            target: this.options.proxy[context],
                        };
                    } else {
                        proxyOptions = Object.assign({}, this.options.proxy[context]);
                        proxyOptions.context = correctedContext;
                    }
                    return proxyOptions;
                });
            }
        }

        const getProxyMiddleware = (proxyConfig) => {
            const context = proxyConfig.context || proxyConfig.path;
            if (proxyConfig.target) {
                return createProxyMiddleware(context, proxyConfig);
            }
            return null;
        };
        /**
         * Assume a proxy configuration specified as:
         * proxy: [
         *   {
         *     context: ...,
         *     ...options...
         *   },
         *   // or:
         *   function() {
         *     return {
         *       context: ...,
         *       ...options...
         *     };
         *   }
         * ]
         */
        this.options.proxy.forEach((proxyConfigOrCallback) => {
            const proxyMiddleware = getProxyMiddleware(proxyConfigOrCallback);

            if (this.options.webSocketServer) {
                this.websocketProxies.push(proxyMiddleware);
            }

            // eslint-disable-next-line @typescript-eslint/require-await
            const handle = async (req: Request, res: Response, next: NextFunction) => {
                if (proxyMiddleware) {
                    const call = proxyMiddleware as any;
                    return call(req, res, next);
                } else {
                    next();
                }
            };

            this.app.use(handle);
            // Also forward error requests to the proxy so it can handle them.
            this.app.use((error, req, res, next) => handle(req, res, next));
        });
    }

    private setupHeadersFeature() {
        this.app.all('*', this.setContentHeaders.bind(this));
    }

    private setupFeatures() {
        const features = {
            compress: () => {
                if (this.options.compress) {
                    this.setupCompressFeature();
                }
            },
            proxy: () => {
                if (this.options.proxy) {
                    this.setupProxyFeature();
                }
            },
            middleware: () => {
                // include our middleware to ensure
                // it is able to handle '/index.html' request after redirect
                this.setupMiddleware();
            },
            headers: () => {
                this.setupHeadersFeature();
            },
        };

        const runnableFeatures = [];

        // compress is placed last and uses unshift so that it will be the first middleware used
        if (this.options.compress) {
            runnableFeatures.push('compress');
        }

        runnableFeatures.push('headers', 'middleware');

        if (this.options.proxy) {
            runnableFeatures.push('proxy', 'middleware');
        }

        if (this.options.onAfterSetupMiddleware) {
            runnableFeatures.push('onAfterSetupMiddleware');
        }

        runnableFeatures.forEach((feature) => {
            features[feature]();
        });
    }

    private setupHttps() {
        // if the user enables http2, we can safely enable https
        if ((this.options.http2 && !this.options.https) || this.options.https === true) {
            this.options.https = {
                requestCert: false,
            };
        }

        if (this.options.https) {
            for (const property of ['ca', 'pfx', 'key', 'cert']) {
                const value = this.options.https[property];
                const isBuffer = value instanceof Buffer;

                if (value && !isBuffer) {
                    let stats = null;

                    try {
                        stats = fs.lstatSync(fs.realpathSync(value)).isFile();
                    } catch (error) {
                        // ignore error
                    }

                    // It is file
                    this.options.https[property] = stats ? fs.readFileSync(path.resolve(value)) : value;
                }
            }

            let fakeCert;

            if (!this.options.https.key || !this.options.https.cert) {
                fakeCert = getCertificate(this.logger);
            }

            this.options.https.key = this.options.https.key || fakeCert;
            this.options.https.cert = this.options.https.cert || fakeCert;
        }
    }

    private createServer() {
        if (this.options.https) {
            if (this.options.http2) {
                // TODO: we need to replace spdy with http2 which is an internal module
                this.server = spdy.createServer(
                    {
                        ...(this.options.https as http.ServerOptions),
                        spdy: {
                            protocols: ['h2', 'http/1.1'],
                        },
                    },
                    this.app
                );
            } else {
                this.server = https.createServer(this.options.https as https.ServerOptions, this.app);
            }
        } else {
            this.server = http.createServer(this.app);
        }

        this.server.on('error', (error) => {
            throw error;
        });
    }

    private createSocketServer() {
        this.socketServer = new WebsocketServer(this);

        this.socketServer.onConnection((connection, headers) => {
            if (!connection) {
                return;
            }

            if (!headers) {
                this.logger.warn(
                    'transportMode.server implementation must pass headers to the callback of onConnection(f) ' +
                        'via f(connection, headers) in order for clients to pass a headers security check'
                );
            }

            if (!headers || !this.checkHost(headers) || !this.checkOrigin(headers)) {
                this.sockWrite([connection], 'error', 'Invalid Host/Origin header');

                this.socketServer.close(connection);

                return;
            }

            this.sockets.push(connection);

            this.socketServer.onConnectionClose(connection, () => {
                const idx = this.sockets.indexOf(connection);

                if (idx >= 0) {
                    this.sockets.splice(idx, 1);
                }
            });

            if (this.options.hot) {
                this.sockWrite([connection], 'hot');
            }
        });
    }

    private setContentHeaders(req: Request, res: Response, next: NextFunction) {
        if (this.options.headers) {
            // eslint-disable-next-line guard-for-in
            for (const name in this.options.headers) {
                res.setHeader(name, this.options.headers[name]);
            }
        }

        next();
    }

    private checkHost(headers: IncomingHttpHeaders) {
        return this.checkHeaders(headers, 'host');
    }

    private checkOrigin(headers: IncomingHttpHeaders) {
        return this.checkHeaders(headers, 'origin');
    }

    private checkHeaders(headers: IncomingHttpHeaders, headerToCheck) {
        if (!headerToCheck) {
            headerToCheck = 'host';
        }

        // get the Host header and extract hostname
        // we don't care about port not matching
        const hostHeader = headers[headerToCheck] as string;

        if (!hostHeader) {
            return false;
        }

        // use the node url-parser to retrieve the hostname from the host-header.
        const hostname = url.parse(
            // if hostHeader doesn't have scheme, add // for parsing.
            /^(.+:)?\/\//.test(hostHeader) ? hostHeader : `//${hostHeader}`,
            false,
            true
        ).hostname;

        // always allow requests with explicit IPv4 or IPv6-address.
        // A note on IPv6 addresses:
        // hostHeader will always contain the brackets denoting
        // an IPv6-address in URLs,
        // these are removed from the hostname in url.parse(),
        // so we have the pure IPv6-address in hostname.
        // always allow localhost host, for convenience (hostname === 'localhost')
        // allow hostname of listening address  (hostname === this.hostname)
        const isValidHostname =
            ipaddr.IPv4.isValid(hostname) ||
            ipaddr.IPv6.isValid(hostname) ||
            hostname === 'localhost' ||
            hostname === this.hostname;

        if (isValidHostname) {
            return true;
        }

        // also allow public hostname if provided
        // if (typeof this.options.public === 'string') {
        //     const idxPublic = this.options.public.indexOf(':');
        //     const publicHostname = idxPublic >= 0 ? this.options.public.substr(0, idxPublic) : this.options.public;
        //     // console.info(`hostname:'${hostname}' === publicHostname:'${publicHostname}'`)
        //     if (hostname === publicHostname) {
        //         return true;
        //     }
        // }

        // disallow
        return false;
    }

    private sockWrite(sockets: WebSocket[], type: 'hot' | 'error', data?: string) {
        sockets.forEach((socket) => {
            this.socketServer.send(socket, JSON.stringify({ type, data }));
        });
    }
}
