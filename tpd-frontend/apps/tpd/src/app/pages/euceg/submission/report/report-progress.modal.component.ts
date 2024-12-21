import { AfterContentInit, Component, OnDestroy } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { ProgressReport, SubmissionReportService } from '@devacfr/euceg';
import { NotifierService } from '@devacfr/layout';
import { BsColor, Progress } from '@devacfr/util';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-report-progress',
    templateUrl: './report-progress.modal.component.html',
})
export class ReportProgressModalComponent implements AfterContentInit, OnDestroy {
    public progress: Progress;
    public progressType: BsColor = 'primary';

    public title: string | undefined;
    public messageStart: string | undefined;
    public messageEnd: string | undefined;

    public cancelToken!: string;

    private _progressReport: ProgressReport;
    private _subscription = new Subscription();

    constructor(
        public svgIcons: SvgIcons,
        private _notifierService: NotifierService,
        private bsModalRef: BsModalRef,
        private _reportService: SubmissionReportService
    ) {
        this._progressReport = new ProgressReport(this._reportService);
        this.progress = {
            percentage: 0,
            message: this.messageStart,
        };
    }

    public ngAfterContentInit(): void {
        this.onInprogress();
    }

    public ngOnDestroy(): void {
        this._subscription.unsubscribe();
    }

    public onInprogress() {
        this._subscription.add(
            this._progressReport.start(this.cancelToken).subscribe({
                next: (progress) => {
                    if (progress.percentage < 100) {
                        this.updateProgress(progress);
                    } else {
                        this.updateProgress({
                            percentage: 100,
                            message: this.messageEnd,
                        });
                    }
                    this.setComplete();
                },
                error: (err) => {
                    if (err.status === 404) {
                        this.updateProgress({
                            percentage: 100,
                            message: this.messageEnd,
                        });
                        this.setComplete();
                    } else {
                        this.setComplete();
                        this._notifierService.error(err);
                    }
                },
            })
        );
    }

    public cancel(): void {
        this._reportService.cancel(this.cancelToken).subscribe({
            next: () => {
                this.setComplete();
            },
            error: (err) => {
                this.setComplete();
                this._notifierService.error(err);
            },
        });
    }

    public updateProgress(progress) {
        this.progress = progress;
        this.progressType = progress < 100 ? 'primary' : 'success';
    }

    public setComplete() {
        setTimeout(() => {
            this.hide();
        }, 1000);
    }

    public hide() {
        this.bsModalRef.hide();
    }
}
