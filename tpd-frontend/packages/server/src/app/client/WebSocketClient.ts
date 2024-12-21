export default class WebSocketClient {
    private client: WebSocket;
    /**
     * @param {string} url
     */
    constructor(url: string | URL) {
        this.client = new WebSocket(url);
        this.client.onerror = (error) => {
            console.error(error);
        };
    }

    /**
     * @param {(...args: any[]) => void} f
     */
    onOpen(f: (this: WebSocket, ev: Event) => unknown) {
        this.client.onopen = f;
    }

    /**
     * @param {(...args: any[]) => void} f
     */
    onClose(f: (this: WebSocket, ev: Event) => unknown) {
        this.client.onclose = f;
    }

    // call f with the message string as the first argument
    /**
     * @param {(...args: any[]) => void} f
     */
    onMessage(f) {
        this.client.onmessage = (e) => {
            f(e.data);
        };
    }
}
