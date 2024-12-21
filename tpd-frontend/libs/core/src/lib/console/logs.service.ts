import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { LogEvents } from '.';
import { BACKEND_SERVER_API_URL_TOKEN } from '../shared';
import { LoggerRequest } from './logger.model';

@Injectable({ providedIn: 'root' })
export class LogsService {
    API_URL: string;

    constructor(
        @Inject(BACKEND_SERVER_API_URL_TOKEN) private BACKEND_SERVER_API_URL: string,
        private _httpClient: HttpClient
    ) {
        this.API_URL = this.BACKEND_SERVER_API_URL + 'logs';
    }

    public findAll(): Observable<LoggerRequest[]> {
        return this._httpClient.get<LoggerRequest[]>(this.API_URL);
    }

    public changeLevel(setting: LoggerRequest): Observable<void> {
        return this._httpClient.put<void>(this.API_URL, setting);
    }

    public getLastLog(): Observable<LogEvents> {
        return this._httpClient.get<LogEvents>(this.API_URL + '/last');
    }
}
