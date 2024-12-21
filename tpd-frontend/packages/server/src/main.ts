import express from 'express';
import minimist from 'minimist';
import { getProxyConfiguration } from '@devacfr/proxy-config';
import { ExecutionMode } from './@types/global';
import { Server } from './app/server';

const defaultOutputPath = '../dist/www';

let outputPath = process.env.OUTPUT_PATH || defaultOutputPath;
let middlewareTarget = process.env.MIDDLEWARE_TARGET;
let mode: ExecutionMode = (process.env.MODE as ExecutionMode) || 'production';

const args = minimist(process.argv.slice(2), {
    string: ['output-path', 'target', 'mode'],
    alias: { o: 'output-path', t: 'target' },
});

if (args['o']) {
    outputPath = args['o'];
}
if (args['t']) {
    middlewareTarget = args['t'];
    process.env.MIDDLEWARE_TARGET = middlewareTarget;
}
if (args['mode']) {
    mode = args['mode'];
}

const cfg = getProxyConfiguration({
    mode,
});
const server = new Server(cfg);
server.use(express.static(outputPath));
server.listen(cfg.port, cfg.host, () => {
    server.logger.info(`Start Server on output path '${outputPath}'`);
    server.logger.info(`Using url 'http://${cfg.host}:${cfg.port}'`);
    server.logger.info(`Target: '${middlewareTarget}'`);
});
