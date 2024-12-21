import rimraf from 'rimraf';
import util from 'util';
import findCacheDir from 'find-cache-dir';
import fs from 'graceful-fs';
import os from 'os';
import pRetry from 'p-retry';
import path from 'path';
import portfinder from 'portfinder';
import selfsigned from 'selfsigned';
import Server, { ClientConfiguration } from 'webpack-dev-server';
import WebpackDevServer from 'webpack-dev-server';

export function tryParseInt(input) {
    const output = parseInt(input, 10);

    if (Number.isNaN(output)) {
        return null;
    }

    return output;
}

export function runPortFinder(): Promise<number> {
    return new Promise((resolve, reject) => {
        // portfinder.basePort = '8080';
        portfinder.getPort((error, port) => {
            if (error) {
                return reject(error);
            }

            return resolve(port);
        });
    });
}

export function findPort(port: Server.Port): Promise<number> {
    if (!port || port === 'auto') {
        // Try to find unused port and listen on it for 3 times,
        // if port is not specified in options.
        // so the tryParseInt function is introduced to handle NaN
        const defaultPortRetry = tryParseInt(process.env.DEFAULT_PORT_RETRY) || 3;

        return pRetry(runPortFinder, { retries: defaultPortRetry });
    } else if (typeof port === 'string') {
        return Promise.resolve(tryParseInt(port));
    } else if (typeof port === 'number') {
        return Promise.resolve(port);
    }
}

export function createCertificate(attributes) {
    return selfsigned.generate(attributes, {
        algorithm: 'sha256',
        days: 30,
        keySize: 2048,
        extensions: [
            // {
            //   name: 'basicConstraints',
            //   cA: true,
            // },
            {
                name: 'keyUsage',
                keyCertSign: true,
                digitalSignature: true,
                nonRepudiation: true,
                keyEncipherment: true,
                dataEncipherment: true,
            },
            {
                name: 'extKeyUsage',
                serverAuth: true,
                clientAuth: true,
                codeSigning: true,
                timeStamping: true,
            },
            {
                name: 'subjectAltName',
                altNames: [
                    {
                        // type 2 is DNS
                        type: 2,
                        value: 'localhost',
                    },
                    {
                        type: 2,
                        value: 'localhost.localdomain',
                    },
                    {
                        type: 2,
                        value: 'lvh.me',
                    },
                    {
                        type: 2,
                        value: '*.lvh.me',
                    },
                    {
                        type: 2,
                        value: '[::1]',
                    },
                    {
                        // type 7 is IP
                        type: 7,
                        ip: '127.0.0.1',
                    },
                    {
                        type: 7,
                        ip: 'fe80::1',
                    },
                ],
            },
        ],
    });
}

export function normalizeOptions(options: WebpackDevServer.Configuration) {
    options.hot = typeof options.hot === 'boolean' || options.hot === 'only' ? options.hot : true;

    // normalize transportMode option
    let transport;
    if (typeof options.webSocketServer === 'undefined') {
        transport = {
            server: 'ws',
            client: 'ws',
        };
    } else {
        switch (typeof options.webSocketServer) {
            case 'string':
                transport = {
                    type: options.webSocketServer,
                };
                break;
            // if not a string, it is an object
            default:
                transport = {
                    type: 'ws',
                };
        }
    }
    options.webSocketServer = transport;

    if (!options.client) {
        options.client = {};
    }
    options.client = {} as ClientConfiguration;

    // Enable client overlay by default
    if (typeof options.client.overlay === 'undefined') {
        options.client.overlay = true;
    }

    let path = 'ws';
    if (typeof options.client.webSocketURL == 'string') {
        path = options.client.webSocketURL;
    }
    options.client.webSocketURL = path.replace(/^\/|\/$/g, '');
    options.devMiddleware = options.devMiddleware || {};
}

export async function getCertificate(logger: Console) {
    // Use a self-signed certificate if no certificate was configured.
    // Cycle certs every 24 hours
    const certificateDir = findCacheDir({ name: 'webpack-dev-server' }) || os.tmpdir();
    const certificatePath = path.join(certificateDir, 'server.pem');

    let certificateExists = fs.existsSync(certificatePath);

    if (certificateExists) {
        const certificateTtl = 1000 * 60 * 60 * 24;
        const certificateStat = fs.statSync(certificatePath);

        const now = new Date();

        // cert is more than 30 days old, kill it with fire
        if ((now.getTime() - certificateStat.ctime.getTime()) / certificateTtl > 30) {
            logger.info('SSL Certificate is more than 30 days old. Removing.');

            const del = util.promisify(rimraf);

            await del(certificatePath, {});

            certificateExists = false;
        }
    }

    if (!certificateExists) {
        logger.info('Generating SSL Certificate');

        const attributes = [{ name: 'commonName', value: 'localhost' }];
        const pems = createCertificate(attributes);

        fs.mkdirSync(certificateDir, { recursive: true });
        fs.writeFileSync(certificatePath, pems.private + pems.cert, {
            encoding: 'utf8',
        });
    }

    return fs.readFileSync(certificatePath);
}
