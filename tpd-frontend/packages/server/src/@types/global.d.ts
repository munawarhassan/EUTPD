export type ExecutionMode = 'production' | 'development' | 'none';

// declare module 'webpack-dev-server' {
//     export interface Configuration {
//         client?: {
//             path?: string;
//             overlay?: boolean;
//         };
//         middlelware?: MiddlewareConfiguration;
//         onBeforeSetupMiddleware?: Function;
//         onAfterSetupMiddleware?: Function;
//     }
// }

// export interface MiddlewareConfiguration {
//     mimeTypes?: unknown;
//     writeToDisk?: boolean | Function;
//     methods?: string[];
//     headers?: unknown;
//     publicPath?: 'auto' | string | Function;
//     serverSideRender?: boolean;
//     outputFileSystem?: unknown;
//     index?: boolean | string;
// }

export interface MiddlewareContext {
    state: boolean;
    callbacks: (() => void)[];
    options: any;
    logger: Console;
    outputFileSystem?: any;
}
