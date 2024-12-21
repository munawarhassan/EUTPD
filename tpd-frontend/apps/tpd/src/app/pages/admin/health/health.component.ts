import { AfterViewInit, ChangeDetectorRef, Component } from '@angular/core';
import { BlockUI, SvgIcons } from '@devacfr/bootstrap';
import { HealthCheckItem, MonitoringService } from '@devacfr/core';
import { NotifierService } from '@devacfr/layout';
import { finalize } from 'rxjs/operators';

@Component({
    selector: 'app-health',
    templateUrl: './health.component.html',
})
export class HealthComponent implements AfterViewInit {
    public healthCheck: { key: string; value: HealthCheckItem }[] | undefined;

    private _block = new BlockUI('#m_portlet_health');
    constructor(
        public svgIcons: SvgIcons,
        private _cd: ChangeDetectorRef,
        private _monitoringService: MonitoringService,
        private _notifierService: NotifierService
    ) {}

    public ngAfterViewInit() {
        this.refresh();
    }

    public refresh() {
        this._block.block();
        this._monitoringService
            .checkHealth()
            .pipe(finalize(() => this._block.release()))
            .subscribe({
                next: (data) => {
                    this.healthCheck = Object.keys(data).map((k) => ({ key: k, value: data[k] }));
                    this._cd.markForCheck();
                },
                error: (err) => this._notifierService.error(err),
            });
    }
}
