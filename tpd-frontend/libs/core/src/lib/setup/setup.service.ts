import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Progress } from '@devacfr/util';
import { interval, Observable } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { DatabaseSetting, DatabaseType, MailSetting } from '../setting';
import { BACKEND_SERVER_API_URL_TOKEN } from '../shared';
import { CreateAdmin } from './create-admin.model';

@Injectable()
export class SetupService {
    private API_URL: string;

    constructor(
        @Inject(BACKEND_SERVER_API_URL_TOKEN) private BACKEND_SERVER_API_URL: string,
        private _httpClient: HttpClient
    ) {
        this.API_URL = this.BACKEND_SERVER_API_URL + 'setup/';
    }

    public markDatabaseAsSetup() {
        sessionStorage.setItem('HAS_SETUP_DB_NAME', 'true');
    }

    public unmarkDatabaseAsSetup() {
        sessionStorage.removeItem('HAS_SETUP_DB_NAME');
    }

    public hasSetupDatabase(): boolean {
        return sessionStorage.getItem('HAS_SETUP_DB_NAME') === 'true';
    }

    public createAdmin(account: CreateAdmin): Observable<any> {
        return this._httpClient.post(this.API_URL + 'createAdmin', account);
    }

    public getGeneralInformation(): Observable<any> {
        return this._httpClient.get(this.API_URL + 'config/general');
    }

    public getMailConfiguration(): Observable<MailSetting> {
        return this._httpClient.get<MailSetting>(this.API_URL + 'config/mail');
    }

    public saveMailConfiguration(mailSetting) {
        return this._httpClient.post(this.API_URL + 'mail', mailSetting);
    }

    public getDatabaseTypes(): Observable<DatabaseType[]> {
        return this._httpClient.get<DatabaseType[]>(this.API_URL + 'databaseTypes');
    }

    public getDefaultExternalDatabase(): Observable<DatabaseType> {
        return this._httpClient.get<DatabaseType>(this.API_URL + 'defaultExternalDatabase');
    }

    public saveDatabaseConfiguration(database: DatabaseSetting, internal: boolean): Observable<any> {
        return this._httpClient.post(this.API_URL + 'database', database, {
            params: {
                internal: internal.toString(),
            },
        });
    }

    public progress(period = 500): Observable<Progress> {
        return interval(period).pipe(switchMap(() => this._httpClient.get<Progress>(this.API_URL + 'progress')));
    }

    public testDatabaseConnection(database: DatabaseSetting): Observable<void> {
        return this._httpClient.post<void>(this.API_URL + 'database/testconnection', database);
    }

    public testMailConnection(mailConfiguration: any, recipient: string): Observable<any> {
        return this._httpClient.post(this.API_URL + 'mail/testconnection', mailConfiguration, {
            params: {
                to: recipient,
            },
        });
    }

    public completeSetup(): Observable<void> {
        return this._httpClient.post<void>(this.API_URL + 'complete', {});
    }
}
