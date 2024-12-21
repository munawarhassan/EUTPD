import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy } from '@angular/core';
import { BlockUI, SvgIcons } from '@devacfr/bootstrap';
import { ReportList, SubmissionReportService } from '@devacfr/euceg';
import { I18nService, NotifierService } from '@devacfr/layout';
import { PageObserver, Pageable } from '@devacfr/util';
import { EMPTY, Observable, Subscription } from 'rxjs';
import { catchError, finalize, switchMap, tap } from 'rxjs/operators';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-report-list',
    templateUrl: 'report-list.component.html',
})
export class ReportListComponent implements OnDestroy {
    public currentPageable: Pageable;
    public tableOptions = ReportList.tableOptions;
    public page: PageObserver<ReportList>;

    private _subscriptions = new Subscription();

    private _block = new BlockUI();

    constructor(
        public svgIcons: SvgIcons,
        private _submissionReportService: SubmissionReportService,
        private _notifierService: NotifierService,
        private _i8n: I18nService
    ) {
        this.currentPageable = Pageable.of(0, 10);
        this.page = (obs: Observable<Pageable>) => {
            return obs.pipe(
                tap(() => {
                    this._block.block();
                }),
                switchMap((pageable) => {
                    return this._submissionReportService.page(pageable).pipe(finalize(() => this._block.release()));
                }),
                catchError((err: HttpErrorResponse) => {
                    // exclude error on search
                    if (err.status !== 400) {
                        this._notifierService.error(err);
                    }
                    return EMPTY;
                })
            );
        };
    }

    ngOnDestroy(): void {
        this._subscriptions.unsubscribe();
    }

    public refresh(): void {
        this.currentPageable = this.currentPageable.first();
    }

    public reportTrack(index: number, report: ReportList): string {
        return report.id;
    }

    public deleteReport(filename: string): void {
        Swal.fire({
            title: 'Are you sure?',
            html: 'Are you sure that you want to delete report file ' + filename + '?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: this._i8n.instant('global.button.delete'),
            cancelButtonText: this._i8n.instant('global.button.cancel'),
        }).then((result) => {
            if (result.isConfirmed) {
                this._block.block();
                this._submissionReportService
                    .delete(filename)
                    .pipe(finalize(() => this._block.release()))
                    .subscribe({
                        next: () => {
                            this._notifierService.success('The report ' + filename + ' has been removed.');
                            this.refresh();
                        },
                        error: (err) => this._notifierService.error(err),
                    });
            }
        });
    }
}
