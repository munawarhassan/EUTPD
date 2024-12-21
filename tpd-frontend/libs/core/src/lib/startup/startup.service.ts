import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { BACKEND_SERVER_URL_TOKEN } from '../shared';
import { TrackerService } from '../tracker/tracker.service';
import { ApplicationEvent, ApplicationState, ApplicationStateInfo, LifecyleProgress } from './types';

@Injectable({
    providedIn: 'root',
})
export class StartupService {
    private started: boolean;
    private trackerStarded: boolean;

    private API_URL: string;

    constructor(
        @Inject(BACKEND_SERVER_URL_TOKEN) private BACKEND_SERVER_URL,
        private _httpClient: HttpClient,
        private _trackerService: TrackerService
    ) {
        this.API_URL = this.BACKEND_SERVER_URL + 'system/';
        this.started = false;
        this.trackerStarded = false;
    }

    public handle(router: Router): Observable<ApplicationStateInfo> {
        return this.info().pipe(
            tap((info) => {
                if (info.status === ApplicationState.Starting && router.url !== '/startup') {
                    // got to startup view
                    router.navigate(['/startup'], { skipLocationChange: true });
                } else if (info.status === ApplicationState.Error && router.url !== '/startup/failed') {
                    // go to setup failed view
                    router.navigate(['/startup/failed']);
                } else if (info.status === ApplicationState.FirstRun && router.url !== '/setup') {
                    // go to setup view
                    router.navigate(['/setup']);
                } else {
                    this.started = info.status === ApplicationState.Running;
                    if (this.started && !this.trackerStarded) {
                        this._trackerService.start();
                        this.trackerStarded = true;
                    }
                }
            })
        );
    }

    public isStarted(): Observable<boolean> {
        return this.info().pipe(map((info) => info.status === ApplicationState.Running));
    }

    public progress(): Observable<LifecyleProgress> {
        return this._httpClient.get<LifecyleProgress>(this.API_URL + 'progress');
    }

    public info(): Observable<ApplicationStateInfo> {
        return this._httpClient.get<ApplicationStateInfo>(this.API_URL + 'info');
    }

    public events(): Observable<ApplicationEvent[]> {
        return this._httpClient.get<ApplicationEvent[]>(this.API_URL + 'events');
    }
}
