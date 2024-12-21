import { Location } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, ViewChild } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { BlockUI, BreadcrumbService, SvgIcons } from '@devacfr/bootstrap';
import {
    EucegService,
    SubmitterDifference,
    SubmitterRequest,
    SubmitterRevision,
    SubmitterService,
} from '@devacfr/euceg';
import { DaterangepickerType } from '@devacfr/forms';
import { I18nService, NotifierService, PortletComponent, TableOptions } from '@devacfr/layout';
import { Order, Pageable, PageObserver } from '@devacfr/util';
import { OutputFormatType } from 'diff2html/lib/types';
import { combineLatest, EMPTY, Observable } from 'rxjs';
import { catchError, finalize, switchMap, tap } from 'rxjs/operators';

@Component({
    selector: 'app-submitter-revision',
    templateUrl: './submitter-revisions.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SubmitterRevisionsComponent {
    public tableOptions: TableOptions = {
        columns: [
            {
                name: 'check',
                sort: false,
                i18n: 'submitters.revision.fields.check',
                class: 'w-50px',
            },
            {
                name: 'version',
                sort: false,
                i18n: 'submitters.revision.fields.version',
            },
            {
                name: 'submitterId',
                sort: false,
                i18n: 'submitters.revision.fields.submitterId',
            },
            {
                name: 'name',
                sort: false,
                i18n: 'submitters.revision.fields.name',
            },
            {
                name: 'country',
                sort: false,
                i18n: 'submitters.revision.fields.country',
            },
            {
                name: 'modifiedDate',
                sort: false,
                i18n: 'submitters.revision.fields.modifiedDate',
            },
            {
                name: 'modifiedBy',
                sort: false,
                i18n: 'submitters.revision.fields.modifiedBy',
            },
        ],
    };

    public selectText = 'Compare Selected Versions';

    public range: DaterangepickerType = {};

    public form: FormGroup;

    public page: PageObserver<SubmitterRevision>;
    public currentPageable: Pageable;

    public diff: SubmitterDifference | undefined;
    public diffOutputFormat: OutputFormatType = 'side-by-side';

    @ViewChild('portletDiff', { static: true })
    public portletDiff!: PortletComponent;

    private _blockPage = new BlockUI();
    private _content: SubmitterRevision[] | undefined;
    private _submitter: SubmitterRequest | undefined;
    public latest: SubmitterRevision | undefined;

    constructor(
        public svgIcons: SvgIcons,
        public euceg: EucegService,
        public _submitterService: SubmitterService,
        private _formBuilder: FormBuilder,
        private _cd: ChangeDetectorRef,
        private _i8n: I18nService,
        private _route: ActivatedRoute,
        private _location: Location,
        private _notifierService: NotifierService,
        private _breadcrumbService: BreadcrumbService
    ) {
        this.form = this._formBuilder.group({
            checkboxes: new FormArray([]),
        });

        this.currentPageable = Pageable.of(0, 20, undefined, undefined, Order.of('DESC', 'version'));

        this.page = (obs: Observable<Pageable>) => {
            return combineLatest([this._route.paramMap, obs]).pipe(
                tap(() => this._blockPage.block()),
                switchMap(([params, pageable]) => {
                    return this._submitterService.revisions(params.get('id') as string, pageable, this.range).pipe(
                        tap((page) => {
                            this._content = page.content;
                            this.checkboxes.clear({ emitEvent: false });
                            page.content.forEach(() => {
                                this.checkboxes.push(this._formBuilder.control(false), { emitEvent: false });
                            });
                            this._cd.markForCheck();
                        }),
                        finalize(() => this._blockPage.release())
                    );
                })
            );
        };

        this._route.paramMap
            .pipe(switchMap((params) => this._submitterService.show(params.get('id') as string)))
            .subscribe((submitter) => {
                this._submitter = submitter;
                this._breadcrumbService.set(
                    '@revision',
                    this._i8n.instant('submitters.revision.title', {
                        submitterId: submitter.submitterId,
                        name: submitter.name,
                    })
                );
            });

        this._route.paramMap
            .pipe(
                switchMap((params) => this._submitterService.latest(params.get('id') as string)),
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

    public get checkboxes(): FormArray {
        return this.form.get('checkboxes') as FormArray;
    }

    public onCheckboxClick(event: Event) {
        const el = event.target as HTMLInputElement;
        if (!el.checked) return;
        if (this.selectedCheckbox.length >= 2) {
            event.preventDefault();
        }
    }

    public onCompare(): void {
        if (!this.latest || !this._content || !this._submitter) return;
        const controls = this.selectedCheckbox;
        this._submitterService
            .compare(
                this._submitter.submitterId,
                controls.length === 1 ? this.latest.id : this.getCheckedRevision(this._content, controls[0]).id,
                controls.length === 1
                    ? this.getCheckedRevision(this._content, controls[0]).id
                    : this.getCheckedRevision(this._content, controls[1]).id
            )
            .subscribe((diff) => {
                this.diff = diff;
                this.portletDiff.display();
                this.portletDiff.fullscreenPortlet('on');
                this._cd.detectChanges();
            });
    }

    public onCloseDiff(): void {
        this.diff = undefined;
        this._cd.detectChanges();
    }

    public goBack(event: Event): void {
        if (event) {
            event.preventDefault();
            event.stopPropagation();
        }
        this._location.back();
    }

    public get selectedCheckbox(): AbstractControl[] {
        return this.checkboxes.controls.filter((ctrl) => ctrl.value === true);
    }

    public isCompareActivated(): boolean {
        const count: number = this.selectedCheckbox.length;

        if (count === 1) {
            this.selectText = 'Compare with current version';
        } else {
            this.selectText = 'Compare selected versions';
        }
        return count > 0;
    }

    public getDiffDescription(): string {
        if (!this._content || !this._submitter || !this.diff) {
            return '';
        }
        const controls = this.selectedCheckbox;
        const rev1 = this.getCheckedRevision(this._content, controls[0]).version;
        const rev2 = controls.length >= 2 ? this.getCheckedRevision(this._content, controls[1]).version : 'CURRENT';
        if (typeof rev2 === 'string' && rev2 === 'CURRENT') {
            // eslint-disable-next-line max-len
            return `Compare the <strong>Current</strong> version of submitter <code class="bg-secondary p-2">${this._submitter.submitterId}</code> with the version <strong>v${rev1}</strong>`;
        }
        // eslint-disable-next-line max-len
        return `Compare the version <strong>v${rev1}</strong> of submitter <code class="bg-secondary p-2">${this._submitter.submitterId}</code> with the version <strong>v${rev2}</strong>`;
    }

    private getCheckedRevision(content: SubmitterRevision[], control: AbstractControl): SubmitterRevision {
        const index = this.checkboxes.controls.indexOf(control);
        return content[index];
    }
}
