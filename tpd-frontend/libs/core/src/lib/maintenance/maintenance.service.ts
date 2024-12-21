import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Progress } from '@devacfr/util';
import { Observable, of } from 'rxjs';
import { DatabaseSetting } from '../setting';
import { BACKEND_SERVER_API_URL_TOKEN } from '../shared';
import { TaskMonitoring } from './task-monitoring.model';

@Injectable({ providedIn: 'any' })
export class MaintenanceService {
    private API_URL: string;
    private count = 0;

    constructor(
        @Inject(BACKEND_SERVER_API_URL_TOKEN) private BACKEND_SERVER_API_URL: string,
        private _httpClient: HttpClient
    ) {
        this.API_URL = this.BACKEND_SERVER_API_URL + 'admin/maintenance';
    }

    public migrate(database: DatabaseSetting): Observable<TaskMonitoring> {
        return this._httpClient.post<TaskMonitoring>(this.API_URL + '/migration', database);
    }

    public index(): Observable<TaskMonitoring> {
        return this._httpClient.post<TaskMonitoring>(this.API_URL + '/index', {});
    }

    public testConnection(database: DatabaseSetting): Observable<void> {
        return this._httpClient.post<void>(this.API_URL + '/database/testconnection', database);
    }

    public progress(): Observable<Progress> {
        return this._httpClient.get<Progress>(this.API_URL + '/progress');
    }

    public progressTest(): Observable<Progress> {
        return of({
            message: 'In progress ' + this.count + '%',
            percentage: ++this.count,
        });
    }
}
