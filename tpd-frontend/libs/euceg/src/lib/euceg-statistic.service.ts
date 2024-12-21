import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BACKEND_SERVER_API_URL_TOKEN } from '../shared';
import { ProductType } from './typing';
import { Pageable } from '@devacfr/util';

export interface CountResult {
    count: number;
    partitions?: Record<string, number>;
}

export interface HistogramResult {
    series: string[];
    data: Record<string, number[]>;
}

export type HistogramInterval = 'day' | 'week' | 'month' | 'year';
export interface HistogramRequest {
    interval: HistogramInterval;
    bounds: number;
}
@Injectable({ providedIn: 'root' })
export class EucegStatisticService {
    private API_URL: string;
    constructor(
        @Inject(BACKEND_SERVER_API_URL_TOKEN) private BACKEND_SERVER_API_URL: string,
        private _httpClient: HttpClient
    ) {
        this.API_URL = this.BACKEND_SERVER_API_URL + 'euceg/stat';
    }

    public countSubmissionByStatus(): Observable<CountResult> {
        return this._httpClient.get<CountResult>(this.API_URL + '/submission/count/byStatus');
    }

    public countAttachmentByStatus(): Observable<CountResult> {
        return this._httpClient.get<CountResult>(this.API_URL + '/attachment/count/byStatus');
    }

    public countProductByPirStatus(producType: ProductType): Observable<CountResult> {
        return this._httpClient.get<CountResult>(this.API_URL + `/product/${producType}/count/byPirStatus`);
    }

    public countProductBySubmissionType(pageable: Pageable): Observable<CountResult> {
        return this._httpClient.get<CountResult>(this.API_URL + `/product/count/bySubmissionType`, {
            params: pageable.httpParams(),
        });
    }

    public getHistogramRecentSubmission(request: HistogramRequest): Observable<HistogramResult> {
        return this._httpClient.post<HistogramResult>(this.API_URL + '/submission/recent', request);
    }

    public getHistogramRecentEcigProduct(request: HistogramRequest): Observable<HistogramResult> {
        return this._httpClient.post<HistogramResult>(this.API_URL + '/product/ecig/recent', request);
    }

    public getHistogramRecentTobaccoProduct(request: HistogramRequest): Observable<HistogramResult> {
        return this._httpClient.post<HistogramResult>(this.API_URL + '/product/tobacco/recent', request);
    }
}
