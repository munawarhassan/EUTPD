import { MiddlewareContext } from '../../../@types/global';
import { createFsFromVolume, Volume } from 'memfs';

export default function setupOutputFileSystem(context: MiddlewareContext) {
    let outputFileSystem;

    if (context.options.outputFileSystem) {
        // eslint-disable-next-line no-shadow
        const { outputFileSystem: outputFileSystemFromOptions } = context.options;

        // Todo remove when we drop webpack@4 support
        if (typeof outputFileSystemFromOptions.join !== 'function') {
            throw new Error('Invalid options: options.outputFileSystem.join() method is expected');
        }

        // Todo remove when we drop webpack@4 support
        if (typeof outputFileSystemFromOptions.mkdirp !== 'function') {
            throw new Error('Invalid options: options.outputFileSystem.mkdirp() method is expected');
        }

        outputFileSystem = outputFileSystemFromOptions;
    } else {
        outputFileSystem = createFsFromVolume(new Volume());
        // outputFileSystem.join = path.join.bind(path);
    }

    // eslint-disable-next-line no-param-reassign
    context.outputFileSystem = outputFileSystem;
}
