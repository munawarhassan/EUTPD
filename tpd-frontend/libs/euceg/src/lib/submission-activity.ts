import { Inject, Injectable } from '@angular/core';
import { WebSocketProvider, WebsocketService } from '@devacfr/core';
import { Observable } from 'rxjs';
import { BACKEND_SERVER_URL_TOKEN } from '../shared';
import { ProductPirStatus, SubmissionStatus } from './typing';

export interface SubmissionProgress {
    submissionId: number;
    progress: number;
    submissionStatus: SubmissionStatus;
    cancelable: boolean;
    exportable: boolean;
    pirStatus?: ProductPirStatus;
}

@Injectable({ providedIn: 'root' })
export class SubmissionActivity {
    private webSocketProvider: WebSocketProvider<SubmissionProgress>;

    constructor(
        @Inject(BACKEND_SERVER_URL_TOKEN) private _BACKEND_SERVER_URL_TOKEN: string,
        private _websocketService: WebsocketService
    ) {
        this.webSocketProvider = this._websocketService.stomp({
            reconnectTimeout: 3000,
            socketUrl: this._BACKEND_SERVER_URL_TOKEN + 'websocket/submissions',
        });
    }

    public watch(): Observable<SubmissionProgress> {
        return this.webSocketProvider.watch('/topic/submissions');
    }
}
