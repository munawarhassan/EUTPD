import { Location } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { BlockUI, BreadcrumbService, SvgIcons } from '@devacfr/bootstrap';
import { MonitoringService } from '@devacfr/core';
import {
    EucegService,
    ProductRequest,
    ProductService,
    SubmissionList,
    SubmissionSendRequest,
    SubmissionService,
    SubmitterRequest,
    SubmitterService,
} from '@devacfr/euceg';
import { I18nService, NotifierService } from '@devacfr/layout';
import { EMPTY } from 'rxjs';
import { finalize, switchMap, tap } from 'rxjs/operators';

type DisableSubmissionType = {
    name: string;
    disabled: boolean;
    value: string;
};

@Component({
    selector: 'app-product-send',
    templateUrl: './product-send.component.html',
    styleUrls: ['product-send.component.scss'],
})
export class ProductSendComponent implements OnInit {
    public domibus;
    public submissionTypes: DisableSubmissionType[] = [];
    public product: ProductRequest | undefined;
    public submitter: SubmitterRequest | undefined;
    public request: Partial<SubmissionSendRequest> = {};
    public latestSubmission: SubmissionList | undefined;
    public child;
    public previousProductId: string | undefined;

    private _blockPage = new BlockUI();

    constructor(
        public svgIcons: SvgIcons,
        public euceg: EucegService,
        private _route: ActivatedRoute,
        private _router: Router,
        private _location: Location,
        private _productService: ProductService,
        private _submitterService: SubmitterService,
        private _submissionService: SubmissionService,
        private _monitorService: MonitoringService,
        private _notifierService: NotifierService,
        private _i8n: I18nService,
        private _breadcrumbService: BreadcrumbService
    ) {
        this.submissionTypes = this.euceg.SubmissionTypes.map((type) =>
            /*
             * TPD-156: Deactivate the limitation selection of the
             * submission type in "Send Product" page.
             */

            // const lastInError = this.latestSubmission != null &&
            //   this.latestSubmission.submissionStatus === 'ERROR' &&
            //   this.latestSubmission.submissionType === type.value;
            // let disabled;
            // let t1;
            // // NEW, the  product must not have been sent or in error with type of  submission NEW as well.
            // if (type.value === '1') {
            //   disabled = this.latestSubmission != null && !lastInError;
            //   t1 = disabled;
            // } else {
            //   disabled = !t1;
            // }

            ({
                name: type.name,
                disabled: false,
                value: type.value,
            })
        );
    }

    ngOnInit() {
        this.refresh();
    }

    refresh() {
        this._monitorService
            .checkHealth()
            .subscribe({ next: (reponse) => (this.domibus = reponse['domibus.ws']), error: () => (this.domibus = {}) });

        this._route.paramMap
            .pipe(
                switchMap((params: ParamMap) => {
                    if (params.has('id')) {
                        this._blockPage.block();
                        return this._productService.show(params.get('id') as string).pipe(
                            tap((product) => {
                                this._breadcrumbService.set('@product', product.productNumber);
                                this._submitterService.show(product.submitterId).subscribe((submitter) => {
                                    this.submitter = submitter;
                                });
                                product.latestSubmission().subscribe({
                                    next: (data) => {
                                        this.latestSubmission = data;
                                        this.update();
                                    },
                                    error: () => {
                                        this.latestSubmission = undefined;
                                        this.update();
                                    },
                                });
                            }),
                            finalize(() => this._blockPage.release())
                        );
                    } else {
                        return EMPTY;
                    }
                })
            )
            .subscribe({
                next: (product) => {
                    this.product = product;
                    this.request = {
                        productNumber: product.productNumber,
                        submissionType: product.submissionType ? product.submissionType : '1',
                        sendType: undefined,
                    };
                    this.previousProductId = undefined;
                },
                error: (err) => this._notifierService.error(err),
            });
    }

    public goBack(): void {
        this._location.back();
    }

    public checkSubmissionType(): boolean {
        if (!this.request || !this.latestSubmission) return false;
        const r = this.request;
        if (r.submissionType === '2') {
            this.previousProductId = this.latestSubmission.productId;
            return true;
        } else {
            this.previousProductId = undefined;
            return false;
        }
    }

    public createSubmission(sendForm) {
        if (sendForm.$invalid) {
            return;
        }
        this.request.sendType = 'MANUAL';

        this._blockPage.block();
        this._submissionService
            .createAndSendSubmission(this.request)
            .pipe(
                finalize(() => {
                    this._blockPage.release();
                })
            )
            .subscribe({
                next: () => {
                    this._router.navigate(['product', 'submissions']);
                },
                error: (err) => {
                    this._notifierService.error(err);
                },
            });
    }

    public sendImmediatProduct(sendForm: NgForm) {
        if (sendForm.invalid) {
            return;
        }
        this.request.sendType = 'IMMEDIAT';
        this._blockPage.block();
        this._submissionService
            .createAndSendSubmission(this.request)
            .pipe(finalize(() => this._blockPage.release()))
            .subscribe({
                next: () => {
                    this._router.navigate(['product', 'submissions']);
                },
                error: (err) => {
                    this._notifierService.error(err);
                },
            });
    }

    public update() {
        this.child =
            this.latestSubmission != null && this.product && this.product.id !== this.latestSubmission.productNumber
                ? this.latestSubmission.productNumber
                : null;

        // check if preferred submission
        // type is acceptable
        const type = this.submissionTypes.find((atype) => atype.name === this.request.submissionType);
        if (type && type.disabled) {
            // reset preferred submission type
            // selected
            this.request.submissionType = undefined;
        }
    }
}
