import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { EMPTY, Observable } from 'rxjs';
import { catchError, shareReplay } from 'rxjs/operators';
import { BACKEND_SERVER_API_URL_TOKEN } from '../shared';
import { GeneralInfo } from './general-info.model';
import { ScmRequest } from './scm-request.model';

@Injectable({ providedIn: 'root' })
export class InfoService {
    constructor(
        @Inject(BACKEND_SERVER_API_URL_TOKEN) private BACKEND_SERVER_API_URL: string,
        private _httpClient: HttpClient
    ) {}

    public getInfo(): Observable<GeneralInfo> {
        return this._httpClient.get<GeneralInfo>(this.BACKEND_SERVER_API_URL + 'info').pipe(
            shareReplay({ refCount: true, bufferSize: 1 }),
            catchError(() => {
                return EMPTY;
            })
        );
    }

    getScmInfo(): Observable<ScmRequest> {
        return this._httpClient
            .get<ScmRequest>(this.BACKEND_SERVER_API_URL + 'info/scm')
            .pipe(shareReplay({ refCount: true, bufferSize: 1 }));
    }
}
