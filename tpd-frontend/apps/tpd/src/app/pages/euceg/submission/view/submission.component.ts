import { Location } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { BlockUI, BreadcrumbService, SvgIcons } from '@devacfr/bootstrap';
import {
    DomibusService,
    ErrorLog,
    EucegService,
    MessageLog,
    ReceiptRequest,
    SubmissionRequest,
    SubmissionService,
    SubmitterRequest,
} from '@devacfr/euceg';
import { NotifierService } from '@devacfr/layout';
import { ClassBuilder, Pageable } from '@devacfr/util';
import { EMPTY, Observable } from 'rxjs';
import { catchError, finalize, map, switchMap, tap } from 'rxjs/operators';

type MessageLogStore = {
    [messageId: string]: Observable<MessageLog[]>;
};

type ErrorLogStore = {
    [messageId: string]: Observable<ErrorLog[]>;
};
@Component({
    selector: 'app-submission',
    templateUrl: './submission.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SubmissionComponent {
    public submission$!: Observable<SubmissionRequest>;
    public submitter$!: Observable<SubmitterRequest>;

    public messageLogs: MessageLogStore = {};

    public errorLogs: ErrorLogStore = {};

    private _block = new BlockUI('#m_submission');

    constructor(
        public svgIcons: SvgIcons,
        private _route: ActivatedRoute,
        private _location: Location,
        public euceg: EucegService,
        private _submissionService: SubmissionService,
        private _domibusService: DomibusService,
        private _notifierService: NotifierService,
        private _breadcrumbService: BreadcrumbService,
        private _cd: ChangeDetectorRef
    ) {
        this.refresh();
    }

    public refresh() {
        this.submission$ = this._route.paramMap.pipe(
            switchMap((params: ParamMap) => {
                if (params.has('id')) {
                    const id = params.get('id') as string;
                    this._block.block();
                    return this._submissionService.show(id).pipe(finalize(() => this._block.release()));
                } else {
                    return EMPTY;
                }
            }),
            tap((submission) => this._breadcrumbService.set('@submission', submission.productId)),
            catchError((err) => {
                this._notifierService.error(err);
                return EMPTY;
            })
        );

        this.submitter$ = this.submission$.pipe(
            switchMap((submission) => submission.submitter()),
            catchError((err) => {
                this._notifierService.error(err);
                return EMPTY;
            })
        );
        this._cd.markForCheck();
    }

    public rejectable(submission: SubmissionRequest): Observable<boolean> {
        return submission.receipts().pipe(
            map((receipts) => {
                const r = receipts.find((r) => r.status === 'PENDING');
                return r != undefined;
            })
        );
    }

    public reject(item: SubmissionRequest) {
        this._block.block();
        this._submissionService
            .rejectSubmission(item.id)
            .pipe(finalize(() => this._block.release()))
            .subscribe({
                next: () => {
                    this.refresh();
                    this._notifierService.success('The submission has been rejected.');
                },
                error: (err) => this._notifierService.error(err),
            });
    }

    public trackReciept(index: number, receipt: ReceiptRequest) {
        return receipt.messageId;
    }

    public handleSelectTab(tab, receipt: ReceiptRequest) {
        const id = tab.id as string;

        if (id.startsWith('m_tab_message_log')) {
            this.refreshMessageLog(receipt);
        } else if (id.startsWith('m_tab_error_log')) {
            this.refreshErrorLog(receipt);
        }
    }

    public refreshMessageLog(receipt: ReceiptRequest) {
        if (!this.messageLogs[receipt.messageId]) {
            const block = new BlockUI(`#m_receipt_${receipt.messageId}`).block();
            this.messageLogs[receipt.messageId] = this._domibusService
                .getMessages(receipt.messageId, Pageable.of(0, 20))
                .pipe(
                    map((page) => page.content),
                    finalize(() => block.release()),
                    catchError((err, caught) => {
                        this._notifierService.error(err);
                        return EMPTY;
                    })
                );
        }
    }

    public refreshErrorLog(receipt: ReceiptRequest) {
        if (!this.errorLogs[receipt.messageId]) {
            const block = new BlockUI(`#m_receipt_${receipt.messageId}`).block();
            this.errorLogs[receipt.messageId] = this._domibusService
                .getErrors(receipt.messageId, Pageable.of(0, 20))
                .pipe(
                    map((page) => page.content),
                    finalize(() => block.release()),
                    catchError((err, caught) => {
                        this._notifierService.error(err);
                        return EMPTY;
                    })
                );
        }
    }

    public getBadgeClasses(receipt: ReceiptRequest) {
        const classes = ClassBuilder.create('badge');
        let color = 'success';
        if (receipt.status === 'CANCELLED') {
            color = 'warning';
        } else if (receipt.error) {
            color = 'danger';
        } else if (receipt.status === 'AWAITING' || receipt.status === 'PENDING') {
            color = 'primary';
        }
        classes.flag('badge-', 'inline', 'bolder', color);
        return classes.toString();
    }

    public getReceiptIcon(receipt: ReceiptRequest) {
        if (receipt.type === 'ATTACHMENT') {
            return 'fa-file-pdf';
        } else if (receipt.type === 'SUBMISSION') {
            return 'fa-arrow-circle-down';
        } else if (receipt.type === 'SUBMITER_DETAILS') {
            return 'fa-building';
        }
        return '';
    }

    public getDescription(receipt: ReceiptRequest) {
        let description = '';
        if (receipt.type === 'ATTACHMENT') {
            description = 'The attachment <strong>' + receipt.name + '</strong> ';
        } else if (receipt.type === 'SUBMISSION') {
            description = 'The submission <strong>' + receipt.name + '</strong> ';
        } else if (receipt.type === 'SUBMITER_DETAILS') {
            description = 'The submitter information <strong>' + receipt.name + '</strong> ';
        }
        if (receipt.status === 'CANCELLED') {
            description += 'was been cancelled.';
        } else if (receipt.status === 'REJECTED') {
            description += 'was been rejected.';
        } else if (receipt.error) {
            description += 'failed.';
        } else if (receipt.status === 'AWAITING' || receipt.status === 'PENDING') {
            description += 'has not been sent.';
        } else {
            description += 'was sent successfully.';
        }
        description += `<span class="ms-4 ${this.getBadgeClasses(receipt)}">${receipt.status}</span>`;
        return description;
    }

    public send(submission: SubmissionRequest) {
        if (submission) {
            this._block.block();
            this._submissionService
                .sendSubmission(submission.id)
                .pipe(finalize(() => this._block.release()))
                .subscribe({
                    next: () => this._notifierService.success('The submission has been sent.'),
                    error: (err) => this._notifierService.error(err),
                });
        }
    }

    public goBack(): void {
        this._location.back();
    }
}
