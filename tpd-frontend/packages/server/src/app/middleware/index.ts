import { NextFunction, Request, Response } from 'express';
import mime from 'mime-types';
import path from 'path';
import { DevMiddlewareOptions } from 'webpack-dev-server';
import { MiddlewareContext } from '../../@types/global';
import getFilenameFromUrl from './utils/getFilenameFromUrl';
import handleRangeHeaders from './utils/handleRangeHeaders';
import ready from './utils/ready';
import setupOutputFileSystem from './utils/setupOutputFileSystem';

export default function middleware(options: DevMiddlewareOptions<any, any> = {}) {
    const context: MiddlewareContext = {
        state: false,
        callbacks: [],
        options,
        logger: null,
    };

    // eslint-disable-next-line no-param-reassign
    context.logger = console;

    setupOutputFileSystem(context);

    const instance = wrapper(context);

    // API
    instance.waitUntilValid = (
        callback = () => {
            /*noop*/
        }
    ) => {
        ready(context, callback);
    };

    instance.context = context;

    return instance;
}

function wrapper(context: MiddlewareContext): {
    (req: Request, res: Response, next: NextFunction): Promise<void>;
    context: MiddlewareContext;
    waitUntilValid: () => void;
} {
    const _f: any = async function middleware(req: Request, res: Response, next: NextFunction): Promise<void> {
        const acceptedMethods = context.options.methods || ['GET', 'HEAD'];

        res.locals = res.locals || {};

        if (!acceptedMethods.includes(req.method)) {
            await goNext();
            return;
        }

        ready(context, processRequest, req);

        async function goNext() {
            if (!context.options.serverSideRender) {
                return next();
            }

            return new Promise((resolve) => {
                ready(
                    context,
                    () => {
                        // eslint-disable-next-line no-param-reassign
                        res.locals.context = { middleware: context };

                        resolve(next());
                    },
                    req
                );
            });
        }

        async function processRequest() {
            const filename = getFilenameFromUrl(context, req.url);
            const { headers } = context.options;
            let content: string | Buffer;

            if (!filename) {
                await goNext();
                return;
            }

            try {
                content = context.outputFileSystem.readFileSync(filename);
            } catch (_ignoreError) {
                await goNext();
                return;
            }

            const contentTypeHeader = res.get ? res.get('Content-Type') : res.getHeader('Content-Type');

            if (!contentTypeHeader) {
                // content-type name(like application/javascript; charset=utf-8) or false
                const contentType = mime.contentType(path.extname(filename));

                // Only set content-type header if media type is known
                // https://tools.ietf.org/html/rfc7231#section-3.1.1.5
                if (contentType) {
                    // Express API
                    if (res.set) {
                        res.set('Content-Type', contentType);
                    }
                    // Node.js API
                    else {
                        res.setHeader('Content-Type', contentType);
                    }
                }
            }

            if (headers) {
                const names = Object.keys(headers);

                for (const name of names) {
                    // Express API
                    if (res.set) {
                        res.set(name, headers[name]);
                    }
                    // Node.js API
                    else {
                        res.setHeader(name, headers[name]);
                    }
                }
            }

            // Buffer
            content = handleRangeHeaders(context, content, req, res);

            // Express API
            if (res.send) {
                res.send(content);
            }
            // Node.js API
            else {
                res.setHeader('Content-Length', content.length);

                if (req.method === 'HEAD') {
                    res.end();
                } else {
                    res.end(content);
                }
            }
        }
    };
    _f.context = context;
    _f.waitUntilValid = null;
    return _f;
}
