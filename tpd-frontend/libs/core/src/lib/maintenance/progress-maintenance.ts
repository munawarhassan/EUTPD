import { Progress } from '@devacfr/util';
import { interval, Observable } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { MaintenanceService } from './maintenance.service';

export class ProgressMaintenance {
    constructor(private _maintenanceService: MaintenanceService) {}

    public start(period = 500): Observable<Progress> {
        return interval(period).pipe(switchMap(() => this._maintenanceService.progress()));
    }

    public test(period = 500): Observable<Progress> {
        return interval(period).pipe(switchMap(() => this._maintenanceService.progressTest()));
    }
}
