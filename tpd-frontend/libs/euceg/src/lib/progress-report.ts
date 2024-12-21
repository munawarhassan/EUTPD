import { Progress } from '@devacfr/util';
import { interval, Observable } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { SubmissionReportService } from './submission-report.service';

export class ProgressReport {
    constructor(private _reportService: SubmissionReportService) {}

    public start(cancelToken: string, period = 500): Observable<Progress> {
        return interval(period).pipe(switchMap(() => this._reportService.progress(cancelToken)));
    }
}
