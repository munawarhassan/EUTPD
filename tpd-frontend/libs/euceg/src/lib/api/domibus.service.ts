import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Page, Pageable } from '@devacfr/util';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { BACKEND_SERVER_API_URL_TOKEN } from '../../shared';
import { ErrorLog } from './error-log.model';
import { MessageLog } from './message-log.model';

@Injectable({ providedIn: 'root' })
export class DomibusService {
    private API_URL: string;
    constructor(
        @Inject(BACKEND_SERVER_API_URL_TOKEN) private BACKEND_SERVER_API_URL: string,
        private _httpClient: HttpClient
    ) {
        this.API_URL = this.BACKEND_SERVER_API_URL + 'domibus';
    }

    public getMessages(conversationId: string, pageable: Pageable): Observable<Page<MessageLog>> {
        return this._httpClient
            .get<Page<MessageLog>>(`${this.API_URL}/${conversationId}/messageLogs`, {
                params: pageable.httpParams(),
            })
            .pipe(Page.mapOf(pageable));
    }

    public getErrors(messageId: string, pageable: Pageable): Observable<Page<ErrorLog>> {
        return this._httpClient
            .get<Page<ErrorLog>>(`${this.API_URL}/${messageId}/errorLogs`, {
                params: pageable.httpParams(),
            })
            .pipe(Page.mapOf(pageable));
    }
}
