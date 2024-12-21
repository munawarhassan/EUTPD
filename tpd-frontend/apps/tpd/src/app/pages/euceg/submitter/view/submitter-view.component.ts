import { Location } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { BlockUI, BreadcrumbService, SvgIcons } from '@devacfr/bootstrap';
import { EucegService, SubmitterRequest, SubmitterService } from '@devacfr/euceg';
import { I18nService, NotifierService, PageChangedEvent } from '@devacfr/layout';
import { DataRow } from '@devacfr/util';
import { BsModalService } from 'ngx-bootstrap/modal';
import { EMPTY, Subscription } from 'rxjs';
import { finalize, switchMap } from 'rxjs/operators';
import { AffiliateModalComponent } from './affiliate-modal.component';

@Component({
    selector: 'app-submitter-view',
    templateUrl: './submitter-view.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SubmitterViewComponent implements OnInit, OnDestroy {
    public maxSize = 10;

    public submitter: SubmitterRequest | undefined;
    public isNewSubmitter = false;
    public readonly = false;
    private subscriptions = new Subscription();

    public dataRows = {
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        affiliate: new DataRow<any>(this, 'submitter.submitter.Affiliates.Affiliate', true),
    };

    private _block = new BlockUI('#m_portlet_submitter');

    constructor(
        public svgIcons: SvgIcons,
        public euceg: EucegService,
        private _route: ActivatedRoute,
        private _router: Router,
        private _location: Location,
        private _notifierService: NotifierService,
        private _i8n: I18nService,
        private _submitterService: SubmitterService,
        private _modalService: BsModalService,
        private _breadcrumbService: BreadcrumbService
    ) {
        this.readonly = this._route.snapshot.data.readOnly;
        this.submitter = this.wrap(this.submitter);
    }

    public ngOnInit() {
        this.refresh();
    }

    public ngOnDestroy(): void {
        this.subscriptions.unsubscribe();
    }

    public refresh(): void {
        this._route.paramMap
            .pipe(
                switchMap((params: ParamMap) => {
                    if (params.has('id')) {
                        this._block.block();
                        this.isNewSubmitter = false;
                        return this._submitterService
                            .show(params.get('id') as string)
                            .pipe(finalize(() => this._block.release()));
                    } else {
                        this.isNewSubmitter = true;
                        return EMPTY;
                    }
                })
            )
            .subscribe({
                next: (submitter) => {
                    if (this.isNewSubmitter) {
                        this._breadcrumbService.set('@submitter', 'New Submitter');
                    } else {
                        this._breadcrumbService.set('@submitter', submitter.name);
                    }
                    this.submitter = this.wrap(submitter);
                    this.dataRows.affiliate.sync();
                },
                error: (err) => this._notifierService.error(err),
            });
    }

    public save(action: string) {
        if (this.isNewSubmitter && this.submitter) {
            // Create
            this._submitterService.create(this.unwrap(this.submitter)).subscribe({
                next: () => {
                    this._notifierService.success('Submitter has been created.');
                    this.isNewSubmitter = false;
                    if (action === 'save-exit') {
                        this.goBack();
                    }
                },
                error: (err) => this._notifierService.error(err),
            });
        } else if (this.submitter) {
            // Update
            this._submitterService.update(this.unwrap(this.submitter)).subscribe({
                next: () => {
                    this._notifierService.success('Submitter has been updated.');
                    this.isNewSubmitter = false;
                    if (action === 'save-exit') {
                        this.goBack();
                    }
                },
                error: (err) => this._notifierService.error(err),
            });
        }
    }

    public onPaginationChanged(ev: PageChangedEvent) {
        this.dataRows.affiliate.pageable = ev.pageable;
    }

    public openAffiliateModal(affiliate) {
        this._modalService.show(AffiliateModalComponent, {
            animated: true,
            backdrop: true,
            initialState: {
                affiliate,
                readonly: this.readonly,
                onClose: () => this.dataRows.affiliate.update(),
                onCancel: () => this.dataRows.affiliate.cancel(),
            },
            class: 'modal-lg modal-dialog-centered',

            providers: [
                {
                    provide: EucegService,
                    useValue: this.euceg,
                },
            ],
        });
    }

    public unwrap(submitter: SubmitterRequest): SubmitterRequest {
        submitter.submitter.submitterID = submitter.submitterId;
        return submitter;
    }

    public goBack(): void {
        this._location.back();
    }

    public goToRevisions(): void {
        if (this.submitter) {
            this._router.navigate(['../../rev/', this.submitter.submitterId], { relativeTo: this._route });
        }
    }

    private wrap(submitter: Partial<SubmitterRequest> | undefined): SubmitterRequest {
        if (!submitter) submitter = {};
        if (!submitter.submitter) {
            submitter.submitter = {};
        }
        if (!submitter.details) {
            submitter.details = {};
        }
        if (!submitter.submitter.Enterer) {
            submitter.submitter.Enterer = {};
        }
        if (!submitter.submitter.Parent) {
            submitter.submitter.Parent = {};
        }
        if (!submitter.submitter.Affiliates) {
            submitter.submitter.Affiliates = {};
        }
        return submitter as SubmitterRequest;
    }
    public addParent() {
        if (this.submitter) {
            this.submitter.submitter.HasParent = true;
            this.submitter.submitter.Parent = {};
        }
    }

    public removeParent() {
        if (this.submitter) {
            this.submitter.submitter.HasParent = false;
            this.submitter.submitter.Parent = undefined;
        }
    }

    public addNaturalLegalRepresentative() {
        if (this.submitter) {
            this.submitter.submitter.HasNaturalLegalRepresentative = { value: true };
            this.submitter.submitter.NaturalLegalRepresentative = {};
        }
    }

    public removeNaturalLegalRepresentative() {
        if (this.submitter) {
            this.submitter.submitter.HasNaturalLegalRepresentative = undefined;
            this.submitter.submitter.NaturalLegalRepresentative = undefined;
        }
    }

    public addEnterer() {
        if (this.submitter) {
            this.submitter.submitter.HasEnterer = true;
            this.submitter.submitter.enterer = {};
        }
    }

    public removeEnterer() {
        if (this.submitter) {
            this.submitter.submitter.HasEnterer = false;
            this.submitter.submitter.enterer = undefined;
        }
    }

    public addAffiliate() {
        if (this.submitter) {
            const submitter = this.submitter.submitter;
            const affiliate = this.dataRows.affiliate.add({});
            submitter.HasAffiliates = true;
            this.openAffiliateModal(affiliate);
        }
    }

    public removeAffiliate(affiliate) {
        if (this.submitter) {
            const submitter = this.submitter.submitter;
            this.dataRows.affiliate.remove(affiliate);
            submitter.HasAffiliates = this.dataRows.affiliate.data.length > 0;
        }
    }
}
