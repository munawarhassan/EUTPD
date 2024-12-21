import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy } from '@angular/core';
import { BlockUI, SvgIcons } from '@devacfr/bootstrap';
import { Channels, TaskMonitoring } from '@devacfr/core';
import {
    EucegService,
    SubmissionActivity,
    SubmissionList,
    SubmissionReportService,
    SubmissionReportType,
    SubmissionService,
} from '@devacfr/euceg';
import { DefaultMenuOptions, I18nService, NotifierService } from '@devacfr/layout';
import { PageObserver, Pageable } from '@devacfr/util';
import { fromNationalMarkets } from '@tpd/app/euceg/components/market-symbol';
import { FilterSubmissionType, SubmissionFilterComponent } from '@tpd/app/euceg/components/submission-filter';

import _, { isArray } from 'lodash-es';
import { BsModalService } from 'ngx-bootstrap/modal';
import { EMPTY, Observable, Subscription } from 'rxjs';
import { catchError, finalize, map, switchMap, tap } from 'rxjs/operators';
import Swal from 'sweetalert2';
import { ReportProgressModalComponent } from './report/report-progress.modal.component';
import { ReportsModalComponent } from './report/reports-modal.component';

interface TripedItem {
    striped: boolean;
}
@Component({
    selector: 'app-submissions',
    templateUrl: './submissions.component.html',
    styleUrls: ['./submissions.component.scss'],
})
export class SubmissionsComponent implements OnDestroy {
    public ReportType = SubmissionReportType;

    public channels = [Channels.SUBMISSION];

    public getCountries = fromNationalMarkets;

    public page: PageObserver<SubmissionList & TripedItem>;
    public currentPageable: Pageable;
    public filters: FilterSubmissionType[] = [];
    public reportMode: SubmissionReportType | undefined;

    public tableOptions = SubmissionList.tableOptions;

    public content: (SubmissionList & TripedItem)[] | undefined;
    public totalElement = 0;

    public currentTask: TaskMonitoring | undefined = undefined;

    private _subscriptions = new Subscription();

    private _block = new BlockUI();

    constructor(
        public svgIcons: SvgIcons,
        public euceg: EucegService,
        private _modalService: BsModalService,
        private _submissionService: SubmissionService,
        private _submissionReportService: SubmissionReportService,
        private _notifierService: NotifierService,
        private _i8n: I18nService,
        private _submissionActivity: SubmissionActivity // _auth: AuthService
    ) {
        this.currentPageable = Pageable.of(0, 20).order().set('lastModifiedDate', 'DESC').end();
        this.page = (obs: Observable<Pageable>) => {
            return obs.pipe(
                tap(() => {
                    this._block.block();
                }),
                switchMap((pageable) => {
                    pageable.clearFilter();

                    this.filters.forEach((filter) => {
                        const f = isArray(filter.filter) ? filter.filter : [filter.filter];
                        pageable.filter().op(filter.property, filter.op, filter.not, ...f);
                    });
                    return this._submissionService.page(pageable).pipe(
                        map((page) => {
                            const p = page.map(
                                (item) => ({ ...item, striped: item.progress === 0.0 } as SubmissionList & TripedItem)
                            );
                            this.content = p.content;
                            this.totalElement = p.totalElements;
                            return p;
                        }),
                        finalize(() => this._block.release())
                    );
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

        this._subscriptions.add(
            this._submissionActivity.watch().subscribe((message) => {
                if (!this.content) {
                    return;
                }

                const id = message.submissionId;
                const item = this.content.find((item) => item.submissionId === id);

                if (item) {
                    item.striped = false;
                    item.progress = message.progress;
                    item.pirStatus = message.pirStatus;

                    if (message.progress <= 1) {
                        item.submissionStatus = message.submissionStatus;
                        item.cancelable = message.cancelable;
                        item.exportable = message.exportable;
                        item.striped = item.progress === 0.0;
                    }
                }
            })
        );
    }

    ngOnDestroy(): void {
        this._subscriptions.unsubscribe();
    }

    public refresh(): void {
        this.currentPageable = this.currentPageable.first();
    }

    public submissionTrack(index: number, submission: SubmissionList): number {
        return submission.submissionId;
    }

    public search(searchTerm: string) {
        this.currentPageable.clearFilter();
        this.currentPageable.search = Pageable.buildQuery(searchTerm);
        this.refresh();
    }

    public onFilterChanged(evt: FilterSubmissionType[]) {
        this.filters = evt;
        this.refresh();
    }

    public clearSearch() {
        this.currentPageable.clearFilter();
        this.currentPageable.search = undefined;
        this.refresh();
    }

    public clearFilters(submissionFilter: SubmissionFilterComponent) {
        this.reportMode = undefined;
        submissionFilter.clearFilters();
    }

    public progress(submission: SubmissionList & TripedItem) {
        if (submission.striped) return 100;
        return Math.floor(submission.progress * 100);
    }

    public send(id) {
        this._block.block();
        this._submissionService
            .sendSubmission(id)
            .pipe(finalize(() => this._block.release()))
            .subscribe({
                next: () => this._notifierService.success('The submission has been sent.'),
                error: (err) => this._notifierService.error(err),
            });
    }

    public cancel(item: SubmissionList) {
        this._block.block();
        this._submissionService
            .cancelSubmission(item.submissionId)
            .pipe(finalize(() => this._block.release()))
            .subscribe({ next: _.noop, error: (err) => this._notifierService.error(err) });
    }

    public enterReport(submissionFilter: SubmissionFilterComponent, reportMode: SubmissionReportType) {
        this.exitReport(submissionFilter);
        this.reportMode = reportMode;
        submissionFilter.store = false;
        switch (reportMode) {
            case 'submission':
                submissionFilter.addFilterStatus('SUBMITTED', true);
                break;
            case 'tobaccoProduct':
                submissionFilter.addFilterStatus('SUBMITTED', true);
                submissionFilter.addFilterProduct('TOBACCO', true);
                submissionFilter.addFilterProductType('11', true, true);
                break;
            case 'novelTobaccoProduct':
                submissionFilter.addFilterStatus('SUBMITTED', true);
                submissionFilter.addFilterProduct('TOBACCO', true);
                submissionFilter.addFilterProductType('11', true); // novel tobacco
                break;
            case 'ecigaretteProduct':
                submissionFilter.addFilterStatus('SUBMITTED', true);
                submissionFilter.addFilterProduct('ECIGARETTE', true);
                break;
            default:
                break;
        }
    }

    public exitReport(submissionFilter: SubmissionFilterComponent) {
        this.clearFilters(submissionFilter);
        submissionFilter.store = true;
    }

    public generateReport(): void {
        if (typeof this.reportMode === 'undefined') {
            return;
        }
        if (this.totalElement > 10000) {
            Swal.fire({
                title: 'Be Careful',
                text: 'The number of Submission size is limited to 10 000.',
                icon: 'warning',
            }).then(() => {
                this.generateReportProgress();
            });
        } else {
            this.generateReportProgress();
        }
    }

    public generateReportProgress(): void {
        if (typeof this.reportMode === 'undefined') {
            return;
        }
        const request = this.currentPageable.copy();
        request.order().clear().set('productNumber', 'ASC', true).set('lastModifiedDate', 'DESC');

        this._submissionReportService.generateReport(this.reportMode, request, this.totalElement).subscribe(
            (task) => {
                this.currentTask = task;
                const modal = this._modalService.show(ReportProgressModalComponent, {
                    initialState: {
                        title: this._i8n.instant('submissions.report.progress.title'),
                        messageStart: this._i8n.instant('submissions.report.progress.message-start'),
                        messageEnd: this._i8n.instant('submissions.report.progress.message-end'),
                        cancelToken: this.currentTask.cancelToken,
                    },
                    ignoreBackdropClick: true,
                    keyboard: false,
                });
                modal.onHide?.subscribe(
                    () => {
                        this.openReportsModal();
                    },
                    (err) => {
                        this._notifierService.error(err);
                    }
                );
            },
            (err) => {
                this._notifierService.error(err);
            }
        );
    }

    public openReportsModal(): void {
        this._modalService.show(ReportsModalComponent, {
            initialState: {
                title: this._i8n.instant('submissions.report.modal.title'),
            },
            ignoreBackdropClick: false,
            keyboard: true,
            class: 'modal-xl modal-dialog-centered',
        });
    }
}
