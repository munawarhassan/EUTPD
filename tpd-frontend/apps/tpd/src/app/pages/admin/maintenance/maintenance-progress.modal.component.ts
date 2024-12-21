import { AfterContentInit, Component, OnDestroy } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { MaintenanceService, ProgressMaintenance } from '@devacfr/core';
import { NotifierService } from '@devacfr/layout';
import { BsColor, Progress } from '@devacfr/util';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-maintenance-progress',
    templateUrl: './maintenance-progress.modal.component.html',
})
export class MaintenanceProgressModalComponent implements AfterContentInit, OnDestroy {
    public progress: Progress;
    public progressType: BsColor = 'primary';

    public title: string | undefined;
    public messageStart: string | undefined;
    public messageEnd: string | undefined;

    private _progressMaintenance: ProgressMaintenance;
    private _subscription = new Subscription();

    constructor(
        public svgIcons: SvgIcons,
        private _notifierService: NotifierService,
        private bsModalRef: BsModalRef,
        private _maintenanceService: MaintenanceService
    ) {
        this._progressMaintenance = new ProgressMaintenance(this._maintenanceService);
        this.progress = {
            percentage: 0,
            message: this.messageStart,
        };
    }

    public ngAfterContentInit(): void {
        this.onInprogress();
        /// this.onTestInprogress();
    }

    public ngOnDestroy(): void {
        this._subscription.unsubscribe();
    }

    public onTestInprogress() {
        this._subscription.add(
            this._progressMaintenance.test().subscribe({
                next: (progress) => {
                    if (progress.percentage < 100) {
                        this.updateProgress(progress);
                    } else {
                        this.updateProgress({
                            percentage: 100,
                            message: 'Test Complete',
                        });
                        this.setComplete();
                    }
                },
                error: (err) => {
                    this.cancel();
                    this._notifierService.error(err);
                },
            })
        );
    }

    public onInprogress() {
        this._subscription.add(
            this._progressMaintenance.start().subscribe({
                next: (progress) => {
                    if (progress.percentage < 100) {
                        this.updateProgress(progress);
                    } else {
                        this.updateProgress({
                            percentage: 100,
                            message: this.messageEnd,
                        });
                        this.setComplete();
                    }
                },
                error: (err) => {
                    if (err.status === 404) {
                        this.updateProgress({
                            percentage: 100,
                            message: this.messageEnd,
                        });
                        this.setComplete();
                    } else {
                        this.cancel();
                        this._notifierService.error(err);
                    }
                },
            })
        );
    }

    public updateProgress(progress) {
        this.progress = progress;
        this.progressType = progress < 100 ? 'primary' : 'success';
    }

    public setComplete() {
        setTimeout(() => {
            this.cancel();
        }, 3000); // 3 sec
    }

    public cancel() {
        this.bsModalRef.hide();
    }
}
