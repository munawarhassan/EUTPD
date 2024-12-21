import { sharedProxy } from './shared-proxy';

describe('sharedProxy', () => {
    it('should work', () => {
        expect(sharedProxy()).toEqual('shared-proxy');
    });
});
