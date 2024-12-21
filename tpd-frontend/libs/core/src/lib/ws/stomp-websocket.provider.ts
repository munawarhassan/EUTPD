import { AuthService } from '@devacfr/auth';
import _ from 'lodash-es';
import { Observable, Observer, Subject } from 'rxjs';
import { map } from 'rxjs/operators';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import { WebSocketConfig } from './types';
import { WebSocketProvider } from './websocket.provider';

export class StompWebSocketProvider<T> implements WebSocketProvider<T> {
    private config: WebSocketConfig;
    private $socket: Promise<Stomp.Client> | undefined;

    constructor(config: WebSocketConfig, private _auth: AuthService) {
        FileList;
        this.config = config;
    }

    public watch(topic: string): Observable<T> {
        this.connect();
        const observable = new Observable((obs: Observer<Stomp.Message>) => {
            if (this.$socket) {
                this.$socket.then((client) => {
                    client.subscribe(topic, obs.next.bind(obs));
                    client.ws.onerror = obs.error.bind(obs);
                    client.ws.close = obs.error.bind(obs);
                    client.ws.onclose = () => {
                        this.reconnect();
                    };
                });
            }
            // on unsubcribe
            return () => {
                this.disconnect();
            };
        });

        const subject: Subject<Stomp.Message> = Subject.create({}, observable);
        return subject.pipe(map((message) => JSON.parse(message.body) as T));
    }

    public publish(broker: string, message: unknown, priority = 9) {
        this.connect();
        if (message) {
            this._auth.prinpalService.identity().subscribe((principal) => {
                const headers: Record<string, unknown> = {
                    priority,
                };
                if (principal && principal.token) {
                    headers['Authorization'] = 'Bearer ' + principal.token;
                }
                if (this.$socket) {
                    this.$socket.then((client) => {
                        client.send(broker, headers, JSON.stringify(message));
                    });
                }
            });
        }
    }

    public connect(): void {
        if (!this.$socket) {
            this.$socket = new Promise((resolve, reject) => {
                const socket = new SockJS(this.config.socketUrl);
                const stomp = Stomp.over(socket);
                const headers: Record<string, unknown> = {};
                if (this._auth) {
                    this._auth.prinpalService.identity().subscribe((principal) => {
                        if (principal && principal.token) {
                            headers['Authorization'] = 'Bearer ' + principal.token;
                        }
                        stomp.connect(
                            headers,
                            () => {
                                resolve(stomp);
                            },
                            (err: unknown) => reject(err)
                        );
                    });
                } else {
                    stomp.connect(
                        headers,
                        () => {
                            resolve(stomp);
                        },
                        (err: unknown) => reject(err)
                    );
                }
            });
        }
    }

    protected disconnect() {
        if (this.$socket != null) {
            this.$socket.then((client) => {
                // eslint-disable-next-line @typescript-eslint/no-empty-function
                client.disconnect(_.noop);
                this.$socket = undefined;
            });
        }
    }

    protected reconnect() {
        setTimeout(() => {
            this.connect();
        }, this.config.reconnectTimeout);
    }
}
