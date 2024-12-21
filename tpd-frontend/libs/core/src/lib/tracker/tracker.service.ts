import { Inject, Injectable, OnDestroy } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { off } from 'process';
import { Observable, of, Subscription } from 'rxjs';
import { BACKEND_SERVER_URL_TOKEN } from '../shared';
import { WebsocketService } from '../ws';
import { WebSocketProvider } from '../ws/websocket.provider';

export interface TrackingActivity {
    sessionId: string;
    userLogin: string;
    ipAddress: string;
    page: string;
    time: string;
}

export interface State {
    name: string;
    id: number;
    url: string;
}

@Injectable({
    providedIn: 'root',
})
export class TrackerService implements OnDestroy {
    private _provider: WebSocketProvider<TrackingActivity> | undefined;

    private _subscriptions = new Subscription();

    constructor(
        @Inject(BACKEND_SERVER_URL_TOKEN) private BACKEND_SERVER_URL,
        private _router: Router,
        private _websocketService: WebsocketService
    ) {}

    public start() {
        if (this._provider) {
            return;
        }
        this._provider = this._websocketService.stomp({
            reconnectTimeout: 3000,
            socketUrl: this.BACKEND_SERVER_URL + 'websocket/activity',
        });
        this._subscriptions.add(
            this._router.events.subscribe((route) => {
                if (route instanceof NavigationEnd) {
                    if (!this._provider) {
                        return;
                    }
                    const r = route;
                    this._provider.publish('/ws/activity', {
                        name: String(r.id),
                        id: r.id,
                        url: r.urlAfterRedirects,
                    });
                }
            })
        );
    }
    ngOnDestroy(): void {
        this._subscriptions.unsubscribe();
    }

    public watch(): Observable<TrackingActivity> {
        if (!this._provider) {
            this.start();
        }
        if (this._provider) {
            return this._provider?.watch('/topic/activity');
        }
        return of();
    }
}
