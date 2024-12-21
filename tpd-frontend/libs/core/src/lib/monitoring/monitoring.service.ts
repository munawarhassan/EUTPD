import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { HealthCheckItem, ThreadDumps } from '.';
import { Metrics } from './metrics.model';

@Injectable({ providedIn: 'any' })
export class MonitoringService {
    constructor(private _httpClient: HttpClient) {}

    public getMetrics(name?: string): Observable<Metrics> {
        return this._httpClient
            .get(
                'metrics/metrics',
                name
                    ? {
                          params: {
                              name,
                          },
                      }
                    : {}
            )
            .pipe(map((value) => new Metrics(value)));
    }

    public checkHealth(): Observable<Record<string, HealthCheckItem>> {
        return this._httpClient.get<Record<string, HealthCheckItem>>('rest/endpoint/health');
    }

    public threadDump(): Observable<ThreadDumps> {
        return this._httpClient.get<ThreadDumps>('metrics/threads');
    }
}
