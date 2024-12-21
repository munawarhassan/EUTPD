import { IncomingHttpHeaders, IncomingMessage } from 'http';
import { ClientConfiguration } from 'webpack-dev-server';
import WebSocket from 'ws';
import { Server } from './server';

export class WebsocketServer {
    private server: Server;
    private wsServer: WebSocket.Server;

    constructor(server: Server) {
        this.server = server;
        const client = this.server.options.client as ClientConfiguration;
        this.wsServer = new WebSocket.Server({
            noServer: true,
            path: client.webSocketURL as string,
        });

        this.server.server.on('upgrade', (req, sock, head) => {
            if (!this.wsServer.shouldHandle(req)) {
                return;
            }

            this.wsServer.handleUpgrade(req, sock, head, (connection: WebSocket) => {
                this.wsServer.emit('connection', connection, req);
            });
        });

        this.wsServer.on('error', (err) => {
            this.server.logger.error(err.message);
        });

        const noop = () => {
            /* noop */
        };

        setInterval(() => {
            this.wsServer.clients.forEach((socket: WebSocket) => {
                const con = socket as any;
                if (con.isAlive === false) {
                    return socket.terminate();
                }

                con.isAlive = false;
                socket.ping(noop);
            });
        }, this.server.wsHeartbeatInterval);
    }

    public send(connection: WebSocket, message: any) {
        // prevent cases where the server is trying to send data while connection is closing
        if (connection.readyState !== 1) {
            return;
        }

        connection.send(message);
    }

    public close(connection: WebSocket) {
        connection.close();
    }

    // f should be passed the resulting connection and the connection headers
    public onConnection(f: (connection: WebSocket, headers: IncomingHttpHeaders) => void) {
        this.wsServer.on('connection', (connection: WebSocket, req: IncomingMessage) => {
            const con = connection as any;
            con.isAlive = true;
            connection.on('pong', () => {
                con.isAlive = true;
            });
            f(connection, req.headers);
        });
    }

    public onConnectionClose(connection: WebSocket, f: (this: WebSocket, code: number, reason: string) => void) {
        connection.on('close', f);
    }
}
