import { Injectable } from '@angular/core';
import { AuthService } from '@devacfr/auth';
import { StompWebSocketProvider } from './stomp-websocket.provider';
import { WebSocketConfig } from './types';
import { WebSocketProvider } from './websocket.provider';

@Injectable({ providedIn: 'root' })
export class WebsocketService {
    constructor(private _auth: AuthService) {}

    public stomp<T>(config: WebSocketConfig): WebSocketProvider<T> {
        const provider = new StompWebSocketProvider<T>(config, this._auth);
        return provider;
    }
}
