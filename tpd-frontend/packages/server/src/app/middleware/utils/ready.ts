import { MiddlewareContext } from '../../../@types/global';
import { Request } from 'express';

export default function ready(context: MiddlewareContext, callback: () => void, req?: Request) {
    const name = (req && req.url) || callback.name;

    context.logger.info(`wait until bundle finished${name ? `: ${name}` : ''}`);

    context.callbacks.push(callback);
}
