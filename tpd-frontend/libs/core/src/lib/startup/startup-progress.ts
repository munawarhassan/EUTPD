import { interval, Observable } from 'rxjs';
import { switchMap } from 'rxjs/operators';

import { StartupService } from './startup.service';
import { LifecyleProgress } from './types';

export class StartupProgress {
    constructor(private _startupService: StartupService) {}

    public start(period = 500): Observable<LifecyleProgress> {
        return interval(period).pipe(switchMap(() => this._startupService.progress()));
    }
}
