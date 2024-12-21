import CopyWebpackPlugin from 'copy-webpack-plugin';
import MergeJsonWebpackPlugin from 'merge-jsons-webpack-plugin';
import webpack from 'webpack';
import WebpackMessages from 'webpack-messages';
import { Configuration as WebpackConfiguration } from 'webpack';
import { Configuration as WebpackDevServerConfiguration } from 'webpack-dev-server';
import { getProxyConfiguration } from '@devacfr/proxy-config';
import { CustomWebpackBrowserSchema, TargetOptions } from '@angular-builders/custom-webpack';

interface Configuration extends WebpackConfiguration {
    devServer?: WebpackDevServerConfiguration;
}

// theme name
const themeName = 'app';

// eslint-disable-next-line @typescript-eslint/no-unused-vars
export default (cfg: Configuration, options: CustomWebpackBrowserSchema, targetOptions: TargetOptions) => {
    const ENV = cfg.mode;

    const DEBUG_INFO_ENABLED = ENV === 'development';
    const MOCK_ENABLED = Object.hasOwnProperty.call(process.env, 'MOCK_ENABLED')
        ? process.env.MOCK_ENABLED
        : DEBUG_INFO_ENABLED;
    // frontend url
    const SERVER_URL = process.env.SERVER_URL || '/';
    // backend url
    const BACKEND_SERVER_URL = process.env.BACKEND_SERVER_URL || '/';
    // bachend api url
    const BACKEND_SERVER_API_URL = BACKEND_SERVER_URL + 'rest/api/';

    const isProd = ENV === 'production';
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const definedVariables: any = {
        DEBUG_INFO_ENABLED,
        MOCK_ENABLED,
        SERVER_URL: JSON.stringify(SERVER_URL),
        BACKEND_SERVER_URL: JSON.stringify(BACKEND_SERVER_URL),
        BACKEND_SERVER_API_URL: JSON.stringify(BACKEND_SERVER_API_URL),
    };
    if (isProd) {
        definedVariables.ngDevMode = !isProd;
    }

    cfg.optimization = {
        minimize: isProd,
    };
    cfg.devServer = getProxyConfiguration(cfg);
    if (!cfg.plugins) {
        cfg.plugins = [];
    }
    cfg.externals = {
        jquery: 'jQuery',
        daterangepicker: 'daterangepicker',
        moment: 'moment',
        apexcharts: 'ApexCharts',
    };
    cfg.plugins.push(
        // webpack log message
        new WebpackMessages({
            name: themeName,
            logger: (str: string) => console.log(`>> ${str}`),
        }),
        new webpack.DefinePlugin(definedVariables),
        new MergeJsonWebpackPlugin({
            output: {
                groupBy: [
                    {
                        pattern: './apps/tpd/src/i18n/en/**/*.json',
                        fileName: './assets/i18n/en.json',
                    },
                    {
                        pattern: './apps/tpd/src/i18n/fr/**/*.json',
                        fileName: './assets/i18n/fr.json',
                    },
                ],
            },
        }),
        new CopyWebpackPlugin({
            patterns: [
                {
                    from: './node_modules/swagger-ui-dist/*.{js,css,html,png}',
                    to: 'swagger-ui/[name][ext]',
                    globOptions: {
                        ignore: ['**/*/index.html'],
                    },
                },
                {
                    from: './apps/tpd/src/swagger-ui/',
                    to: 'swagger-ui',
                },
            ],
        })
    );
    cfg.node = {
        global: true,
    };

    cfg.resolve = {
        extensions: ['.ts', '.js'],
        fallback: {
            net: false,
        },
    };

    return cfg;
};
