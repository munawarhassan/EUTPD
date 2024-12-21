import * as path from 'path';

const EVENT = process.env.npm_lifecycle_event || '';

/**
 * Helper functions.
 */
const ROOT = path.resolve(__dirname, '..');

// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace helpers {
    export function hasProcessFlag(flag: string) {
        return process.argv.join('').indexOf(flag) > -1;
    }

    export function hasNpmFlag(flag: string) {
        return EVENT.includes(flag);
    }

    export function isWebpackDevServer() {
        return process.argv[1] && !!/webpack-dev-server/.exec(process.argv[1]);
    }

    export const root = path.join.bind(path, ROOT);
}
