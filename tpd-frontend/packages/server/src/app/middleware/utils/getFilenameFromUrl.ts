import path from 'path';
import querystring from 'querystring';
import { parse } from 'url';
import { Url } from 'url';
import mem from 'mem';
import { MiddlewareContext } from '../../../@types/global';
const memoizedParse = mem(parse);

export default function getFilenameFromUrl(context: MiddlewareContext, url: string): string {
    const { options } = context;
    const paths = [];

    let filename: string;
    let urlObject;

    try {
        // The `url` property of the `request` is contains only  `pathname`, `search` and `hash`
        urlObject = memoizedParse(url, false, true);
    } catch (_ignoreError) {
        return filename;
    }

    for (const { publicPath, outputPath } of paths) {
        let publicPathObject: Url;

        try {
            publicPathObject = memoizedParse(publicPath !== 'auto' && publicPath ? publicPath : '/', false, true);
        } catch (_ignoreError) {
            // eslint-disable-next-line no-continue
            continue;
        }

        if (urlObject.pathname && urlObject.pathname.startsWith(publicPathObject.pathname)) {
            filename = outputPath;

            // Strip the `pathname` property from the `publicPath` option from the start of requested url
            // `/complex/foo.js` => `foo.js`
            const pathname = urlObject.pathname.substr(publicPathObject.pathname.length);

            if (pathname) {
                filename = path.join(outputPath, querystring.unescape(pathname));
            }

            let fsStats;

            try {
                fsStats = context.outputFileSystem.statSync(filename);
            } catch (_ignoreError) {
                // eslint-disable-next-line no-continue
                continue;
            }

            if (fsStats.isFile()) {
                break;
            } else if (fsStats.isDirectory() && (typeof options.index === 'undefined' || options.index)) {
                const indexValue =
                    typeof options.index === 'undefined' || typeof options.index === 'boolean'
                        ? 'index.html'
                        : options.index;

                filename = path.join(filename, indexValue);

                try {
                    fsStats = context.outputFileSystem.statSync(filename);
                } catch (__ignoreError) {
                    // eslint-disable-next-line no-continue
                    continue;
                }

                if (fsStats.isFile()) {
                    break;
                }
            }
        }
    }

    return filename;
}
