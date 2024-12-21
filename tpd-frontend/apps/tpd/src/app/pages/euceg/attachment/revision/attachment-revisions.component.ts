import { Location } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { BlockUI, BreadcrumbService, SvgIcons } from '@devacfr/bootstrap';
import { AttachmentRequest, AttachmentRevision, AttachmentService } from '@devacfr/euceg';
import { DaterangepickerType } from '@devacfr/forms';
import { I18nService, NotifierService, TableOptions } from '@devacfr/layout';
import { Order, Pageable, PageObserver } from '@devacfr/util';
import { combineLatest, EMPTY, Observable } from 'rxjs';
import { catchError, finalize, switchMap, tap } from 'rxjs/operators';

@Component({
    // eslint-disable-next-line @angular-eslint/component-selector
    selector: 'attachment-revision',
    templateUrl: './attachment-revisions.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AttachmentRevisionsComponent {
    public tableOptions: TableOptions = {
        columns: [
            {
                name: 'version',
                sort: false,
                i18n: 'attachments.revision.version',
            },
            {
                name: 'filename',
                sort: false,
                i18n: 'attachments.revision.filename',
            },
            {
                name: 'confidential',
                sort: false,
                i18n: 'attachments.revision.confidential',
            },
            {
                name: 'modifiedBy',
                sort: false,
                i18n: 'attachments.revision.modifiedBy',
            },
            {
                name: 'modifiedDate',
                sort: false,
                i18n: 'attachments.revision.modifiedDate',
            },
        ],
    };

    public latest: AttachmentRevision | undefined;
    public range: DaterangepickerType = {};

    public page: PageObserver<AttachmentRevision>;
    public currentPageable: Pageable;

    private _block = new BlockUI('#m_portlet_attachment_revision');
    private _attachment: AttachmentRequest | undefined;

    constructor(
        public svgIcons: SvgIcons,
        private _formBuilder: FormBuilder,
        private _route: ActivatedRoute,
        private _location: Location,
        private _cd: ChangeDetectorRef,
        private _attachmentService: AttachmentService,
        private _i8n: I18nService,
        private _notifierService: NotifierService,
        private _breadcrumbService: BreadcrumbService
    ) {
        this.currentPageable = Pageable.of(0, 20, undefined, undefined, Order.of('DESC', 'lastModifiedDate'));

        this.page = (obs: Observable<Pageable>) => {
            return combineLatest([this._route.paramMap, obs]).pipe(
                tap(() => this._block.block()),
                switchMap(([params, pageable]) => {
                    return this._attachmentService
                        .revisions(params.get('attachment') as string, pageable, this.range)
                        .pipe(finalize(() => this._block.release()));
                })
            );
        };

        this._route.paramMap
            .pipe(switchMap((params) => this._attachmentService.show(params.get('attachment') as string)))
            .subscribe((attachment) => {
                this._attachment = attachment;
                this._breadcrumbService.set('@revision', attachment.filename);
            });

        this._route.paramMap
            .pipe(
                switchMap((params) => this._attachmentService.latestRevision(params.get('attachment') as string)),
                catchError((error) => {
                    if (error.status !== 404) this._notifierService.error(error);
                    return EMPTY;
                })
            )
            .subscribe((rev) => {
                this.latest = rev;
                _cd.markForCheck();
            });
    }

    public goBack(event: Event): void {
        if (event) {
            event.preventDefault();
            event.stopPropagation();
        }
        this._location.back();
    }
}
