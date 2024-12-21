import SockJS from 'sockjs-client';

export default class SockJsClient {
    private sock: WebSocket;

    /**
     * @param {string} url
     */
    constructor(url: string) {
        // SockJS requires `http` and `https` protocols
        this.sock = new SockJS(url.replace(/^ws:/i, 'http:').replace(/^wss:/i, 'https:'));
        this.sock.onerror =
            /**
             * @param {Error} error
             */
            (error) => {
                console.error(error);
            };
    }

    /**
     * @param {(...args: any[]) => void} f
     */
    onOpen(f: (this: WebSocket, ev: Event) => unknown) {
        this.sock.onopen = f;
    }

    /**
     * @param {(...args: any[]) => void} f
     */
    onClose(f: (this: WebSocket, ev: Event) => unknown) {
        this.sock.onclose = f;
    }

    // call f with the message string as the first argument
    /**
     * @param {(...args: any[]) => void} f
     */
    onMessage(f) {
        this.sock.onmessage =
            /**
             * @param {Error & { data: string }} e
             */
            (e) => {
                f(e.data);
            };
    }
}
