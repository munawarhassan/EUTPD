import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Page, Pageable } from '@devacfr/util';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { AliasResponse, KeystoreRequest } from '.';
import { BACKEND_SERVER_API_URL_TOKEN } from '../shared';

@Injectable({ providedIn: 'any' })
export class KeystoreService {
    private API_URL: string;
    constructor(
        @Inject(BACKEND_SERVER_API_URL_TOKEN) private BACKEND_SERVER_API_URL: string,
        private _httpClient: HttpClient
    ) {
        this.API_URL = BACKEND_SERVER_API_URL + 'keystores';
    }

    public findAll(pageable: Pageable): Observable<Page<KeystoreRequest>> {
        return this._httpClient
            .get<Page<KeystoreRequest>>(this.API_URL, {
                params: pageable.httpParams(),
            })
            .pipe(Page.mapOf(pageable));
    }

    public delete(alias: string): Observable<any> {
        return this._httpClient.delete(`${this.API_URL}/${alias}`);
    }

    public get(alias: string): Observable<KeystoreRequest> {
        return this._httpClient.get<KeystoreRequest>(`${this.API_URL}/${alias}`);
    }

    public exists(alias: string): Observable<boolean> {
        return this._httpClient.get(`${this.API_URL}/${alias}`, { observe: 'response' }).pipe(
            map((resp) => resp.status === 200),
            catchError(() => of(false))
        );
    }

    public validateCertificate(file: File): Observable<any> {
        const formData = new FormData();
        formData.append('file', file);
        return this._httpClient.post(`${this.API_URL}/certificate/validate`, formData, {
            responseType: 'text',
        });
    }

    public importCertificate(file: File, alias: string): Observable<any> {
        const formData = new FormData();
        formData.append('alias', alias);
        formData.append('file', file);
        return this._httpClient.post(`${this.API_URL}/certificate`, formData);
    }

    public validateKeyPair(file: File, password: string): Observable<AliasResponse> {
        const formData = new FormData();
        formData.append('password', password);
        formData.append('file', file);
        return this._httpClient.post<AliasResponse>(`${this.API_URL}/keypair/validate`, formData);
    }

    public importKeyPair(file: File, alias: string, password: string): Observable<any> {
        const formData = new FormData();
        formData.append('alias', alias);
        formData.append('password', password);
        formData.append('file', file);
        return this._httpClient.post(`${this.API_URL}/keypair`, formData);
    }

    public getSettings(): Observable<any> {
        return this._httpClient.get(`${this.API_URL}/settings`);
    }

    public saveSettings(settings): Observable<any> {
        return this._httpClient.post(`${this.API_URL}/settings`, settings);
    }
}
