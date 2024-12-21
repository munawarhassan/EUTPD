import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { DatabaseConfig, DatabaseConnection, DatabaseType } from './database-setting';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { MailSetting } from './mail-setting';
import { DomibusSetting } from './domibus-setting';
import { BACKEND_SERVER_API_URL_TOKEN } from '../shared';

export interface Host {
    scheme: string;
    hostname: string;
    contextPath?: string;
}

@Injectable({ providedIn: 'root' })
export class ConfigService {
    private API_URL: string;

    constructor(
        @Inject(BACKEND_SERVER_API_URL_TOKEN) private BACKEND_SERVER_API_URL: string,
        private _httpClient: HttpClient
    ) {
        this.API_URL = this.BACKEND_SERVER_API_URL + 'admin/config';
    }

    get contextPath() {
        let path = document.URL;
        path = path.split('#')[0]; // remove fragment
        const i = path.indexOf('/', path.indexOf('//') + 2);
        let end = path.indexOf('/', i + 1);
        if (end === -1) {
            end = i;
        }
        return path.substring(i, end);
    }

    public host(): Observable<Host> {
        return this._httpClient.get<Host>(this.BACKEND_SERVER_API_URL + 'secure/host').pipe(
            map((host) => {
                host.contextPath = this.contextPath;
                return host;
            })
        );
    }

    public getGeneralSetting(): Observable<unknown> {
        return this._httpClient.get(this.API_URL + '/general');
    }

    public getDatabaseSetting(): Observable<DatabaseConfig> {
        return this._httpClient.get<DatabaseConfig>(this.API_URL + '/database');
    }

    public getCurrentDatabase(): Observable<DatabaseConnection> {
        return this._httpClient.get<DatabaseConnection>(this.API_URL + '/database/current');
    }

    public getMailSetting(): Observable<MailSetting> {
        return this._httpClient.get<MailSetting>(this.API_URL + '/mail');
    }

    public saveGeneralSettings(settings: unknown): Observable<unknown> {
        return this._httpClient.post(this.API_URL + '/general', settings);
    }

    public saveMailSetting(settings: MailSetting): Observable<unknown> {
        return this._httpClient.post(this.API_URL + '/mail', settings);
    }

    public mailTestConnection(mail: MailSetting, to: string): Observable<unknown> {
        return this._httpClient.post(this.API_URL + '/mail/test', mail, {
            params: {
                to,
            },
        });
    }

    public getSupportedDatabaseTypes(): Observable<DatabaseType[]> {
        return this._httpClient.get<DatabaseType[]>(this.API_URL + '/supportedDatabaseTypes');
    }

    public getDefaultSupportedDatabase(): Observable<DatabaseType> {
        return this._httpClient.get<DatabaseType>(this.API_URL + '/defaultSupportedDatabase');
    }

    public getLdapSetting(): Observable<unknown> {
        return this._httpClient.get(this.API_URL + '/ldap');
    }

    public saveLdapSetting(settings: unknown): Observable<unknown> {
        return this._httpClient.post(this.API_URL + '/ldap', settings);
    }

    public ldapTestConnection(setting: unknown): Observable<unknown> {
        return this._httpClient.post(this.API_URL + '/ldap/test', setting);
    }

    public getDomibusSetting(): Observable<DomibusSetting> {
        return this._httpClient.get<DomibusSetting>(this.API_URL + '/domibus');
    }

    public saveDomibusSetting(settings: DomibusSetting): Observable<DomibusSetting> {
        return this._httpClient.post<DomibusSetting>(this.API_URL + '/domibus', settings);
    }

    public domibusHealthCheck(healthCheckUrl: string): Observable<boolean> {
        if (!healthCheckUrl) {
            return of(false);
        }
        return this._httpClient
            .get(this.API_URL + '/domibus/healthcheck', {
                observe: 'response',
                params: {
                    url: healthCheckUrl,
                },
            })
            .pipe(
                map((resp) => resp.status === 200),
                catchError(() => of(false))
            );
    }
}
