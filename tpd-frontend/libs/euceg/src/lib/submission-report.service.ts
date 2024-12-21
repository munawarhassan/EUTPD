import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { TaskMonitoring } from '@devacfr/core';
import { Page, Pageable, Progress } from '@devacfr/util';
import { Observable } from 'rxjs';
import { BACKEND_SERVER_API_URL_TOKEN } from '../shared';
import { SubmissionReportType } from './typing';
import { ReportList } from './report.model';

@Injectable({ providedIn: 'root' })
export class SubmissionReportService {
    private API_URL: string;
    constructor(
        @Inject(BACKEND_SERVER_API_URL_TOKEN) private BACKEND_SERVER_API_URL: string,
        private _httpClient: HttpClient
    ) {
        this.API_URL = this.BACKEND_SERVER_API_URL + 'submissions/reporting';
    }

    public generateReport(
        reportMode: SubmissionReportType,
        pageable: Pageable,
        limit = -1
    ): Observable<TaskMonitoring> {
        let params = pageable.httpParams();
        if (limit > 0) {
            params = params.append('limit', limit);
        }
        return this._httpClient.post<TaskMonitoring>(`${this.API_URL}/${reportMode}/report`, null, {
            reportProgress: true,
            params,
        });
    }

    public progress(cancelToken: string): Observable<Progress> {
        return this._httpClient.get<Progress>(`${this.API_URL}/progress/${cancelToken}`);
    }

    public cancel(cancelToken: string): Observable<any> {
        return this._httpClient.get(`${this.API_URL}/cancel/${cancelToken}`);
    }

    public page(request: Pageable): Observable<Page<ReportList>> {
        return this._httpClient
            .get<Page<ReportList>>(`${this.API_URL}/reports`, {
                params: request.httpParams(),
            })
            .pipe(Page.mapOf(request));
    }

    public delete(filename: string): Observable<any> {
        return this._httpClient.delete(`${this.API_URL}/reports/${filename}`);
    }
}
