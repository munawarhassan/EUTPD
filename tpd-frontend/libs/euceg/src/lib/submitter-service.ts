import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Hateoas, HateoasResponse, Page, Pageable } from '@devacfr/util';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { BACKEND_SERVER_API_URL_TOKEN } from '../shared';
import { SubmitterList } from './submitter-list.model';
import { SubmitterRequest } from './submitter-request.model';
import { SubmitterDifference, SubmitterRevision } from './typing';

@Injectable({ providedIn: 'root' })
export class SubmitterService {
    private API_URL: string;
    constructor(
        @Inject(BACKEND_SERVER_API_URL_TOKEN) private BACKEND_SERVER_API_URL: string,
        private _httpClient: HttpClient
    ) {
        this.API_URL = this.BACKEND_SERVER_API_URL + 'submitters';
    }

    public page(pageable: Pageable): Observable<Page<SubmitterList>> {
        return this._httpClient
            .get<HateoasResponse<SubmitterList>>(this.API_URL, {
                params: pageable.httpParams(),
            })
            .pipe(Hateoas.page(this._httpClient, pageable));
    }

    public show(submitterId: string): Observable<SubmitterRequest> {
        return this._httpClient
            .get<SubmitterRequest>(`${this.API_URL}/${submitterId}`)
            .pipe(Hateoas.resource(this._httpClient));
    }

    public revisions(
        submitterId: string,
        pageable: Pageable,
        range?: { startDate?: Date; endDate?: Date }
    ): Observable<Page<SubmitterRevision>> {
        return this._httpClient
            .get<Page<SubmitterRevision>>(`${this.API_URL}/${submitterId}/rev`, {
                params: pageable.httpParams(),
            })
            .pipe(Page.mapOf(pageable));
    }

    public latest(submitterId: string): Observable<SubmitterRevision> {
        return this._httpClient.get<SubmitterRevision>(`${this.API_URL}/${submitterId}/rev/latest`);
    }

    public compare(
        submitterId: string,
        originalRevision: number,
        revisedRevision: number
    ): Observable<SubmitterDifference> {
        return this._httpClient.get<SubmitterDifference>(`${this.API_URL}/${submitterId}/compare`, {
            params: {
                originalRevision: String(originalRevision),
                revisedRevision: String(revisedRevision),
            },
        });
    }

    public create(submitter: SubmitterRequest): Observable<any> {
        return this._httpClient.post(`${this.API_URL}/${submitter.submitterId}`, submitter);
    }

    public update(submitter: SubmitterRequest): Observable<any> {
        return this._httpClient.put(`${this.API_URL}/${submitter.submitterId}`, submitter);
    }

    public delete(submitterId: string): Observable<any> {
        return this._httpClient.delete(`${this.API_URL}/${submitterId}`);
    }

    public importSubmitter(file: File): Observable<any> {
        const formData = new FormData();
        formData.append('file', file);
        return this._httpClient.post(`${this.API_URL}/import`, formData);
    }
}
