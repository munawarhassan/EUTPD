import { Configuration } from 'webpack';
import WebpackDevServer from 'webpack-dev-server';
import { helpers } from './config/helpers';

export function getProxyConfiguration(cfg: Configuration): WebpackDevServer.Configuration {
    const HOST = process.env.HOST || 'localhost';
    const PORT = process.env.PORT ? parseInt(process.env.PORT, 10) : 3030;
    const MIDDLEWARE_TARGET = process.env.MIDDLEWARE_TARGET || 'http://127.0.0.1:8080';
    const HMR = helpers.hasProcessFlag('hot');
    const isProd = cfg.mode === 'production';
    return {
        client: {
            overlay: {
                errors: true,
                warnings: false,
            },
        },
        webSocketServer: 'sockjs',
        historyApiFallback: true,
        port: PORT,
        host: HOST,
        hot: HMR,
        compress: isProd,
        proxy: [
            {
                context: ['/logs', '/rest/api', '/rest', '/auth', '/system', '/metrics', '/system'],
                target: MIDDLEWARE_TARGET,
                secure: false,
            },
            {
                context: ['/websocket'],
                target: MIDDLEWARE_TARGET,
                ws: true,
                secure: false,
            },
        ],
    };
}
