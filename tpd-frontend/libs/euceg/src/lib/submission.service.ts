import { HttpClient, HttpResponse } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Hateoas, HateoasResponse, Page, Pageable } from '@devacfr/util';
import { Observable, Subscription } from 'rxjs';
import { BACKEND_SERVER_API_URL_TOKEN } from '../shared';
import { SubmissionList } from './submission-list.model';
import { SubmissionRequest } from './submission-request';
import { BulkRequest, ProductType, SubmissionReportType } from './typing';
import { finalize } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class SubmissionService {
    private API_URL: string;
    constructor(
        @Inject(BACKEND_SERVER_API_URL_TOKEN) private BACKEND_SERVER_API_URL: string,
        private _httpClient: HttpClient
    ) {
        this.API_URL = this.BACKEND_SERVER_API_URL + 'submissions';
    }

    public page(pageable: Pageable): Observable<Page<SubmissionList>> {
        return this._httpClient
            .get<HateoasResponse<SubmissionList>>(this.API_URL, {
                params: pageable.httpParams(),
            })
            .pipe(Hateoas.page(this._httpClient, pageable));
    }

    public show(id: string): Observable<SubmissionRequest> {
        return this._httpClient
            .get<SubmissionRequest>(`${this.API_URL}/${id}`)
            .pipe(Hateoas.resource(this._httpClient));
    }

    public create(submission: SubmissionRequest): Observable<any> {
        return this._httpClient.post(this.API_URL, submission);
    }

    public update(submission: SubmissionRequest): Observable<any> {
        return this._httpClient.put(`${this.API_URL}/${submission.id}`, submission);
    }

    public delete(id: number): Observable<any> {
        return this._httpClient.delete(`${this.API_URL}/${id}`);
    }

    public createAndSendSubmission(request): Observable<any> {
        return this._httpClient.post(`${this.API_URL}/send`, request);
    }

    public sendSubmission(id: number): Observable<any> {
        return this._httpClient.post(`${this.API_URL}/${id}/send`, {});
    }

    public bulkSendSubmissions(productType: ProductType, request: BulkRequest): Observable<any> {
        return this._httpClient.post(`${this.API_URL}/${productType}/bulkSend`, request);
    }

    public cancelSubmission(submissionId: number): Observable<any> {
        return this._httpClient.put(`${this.API_URL}/${submissionId}/cancel`, {});
    }

    public rejectSubmission(submissionId: number): Observable<any> {
        return this._httpClient.put(`${this.API_URL}/${submissionId}/reject`, {});
    }
}
