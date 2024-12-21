import { Location } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, ViewChild } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { BlockUI, BreadcrumbService, SvgIcons } from '@devacfr/bootstrap';
import { ProductRequest, ProductRevision, ProductRevisionDiffItem, ProductService, ProductType } from '@devacfr/euceg';
import { DaterangepickerType } from '@devacfr/forms';
import { I18nService, NotifierService, PortletComponent, TableOptions } from '@devacfr/layout';
import { Order, Pageable, PageObserver } from '@devacfr/util';
import { OutputFormatType } from 'diff2html/lib/types';
import { combineLatest, EMPTY, Observable } from 'rxjs';
import { catchError, finalize, switchMap, tap } from 'rxjs/operators';

@Component({
    selector: 'app-product-revision',
    templateUrl: './product-revisions.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductRevisionsComponent {
    public tableOptions: TableOptions = {
        columns: [
            {
                name: 'check',
                sort: false,
                i18n: 'products.revision.fields.check',
            },
            {
                name: 'revision',
                sort: false,
                i18n: 'products.revision.fields.revision',
            },
            {
                name: 'pirStatus',
                sort: false,
                align: 'center',
                i18n: 'products.revision.fields.pirStatus',
            },
            {
                name: 'productNumber',
                sort: false,
                i18n: 'products.revision.fields.productNumber',
            },
            {
                name: 'status',
                sort: false,
                align: 'center',
                i18n: 'products.revision.fields.status',
            },
            {
                name: 'modifiedDate',
                sort: false,
                i18n: 'products.revision.fields.modifiedDate',
            },
            {
                name: 'modifiedBy',
                sort: false,
                i18n: 'products.revision.fields.modifiedBy',
            },
        ],
    };

    public selectText = 'Compare Selected Versions';

    public range: DaterangepickerType = {};

    public productType: ProductType;
    private _product: ProductRequest | undefined;

    public page: PageObserver<ProductRevision>;
    public currentPageable: Pageable;
    public latest: ProductRevision | undefined;

    public form: FormGroup;

    @ViewChild('portletDiff', { static: true })
    public portletDiff!: PortletComponent;

    public diff: ProductRevisionDiffItem | undefined = undefined;
    public diffOutputFormat: OutputFormatType = 'side-by-side';

    private _content: ProductRevision[] | undefined;
    private _blockPage = new BlockUI();

    constructor(
        public svgIcons: SvgIcons,
        private _route: ActivatedRoute,
        private _location: Location,
        private _formBuilder: FormBuilder,
        private _productService: ProductService,
        private _notifierService: NotifierService,
        private _i8n: I18nService,
        private _cd: ChangeDetectorRef,
        private _breadcrumbService: BreadcrumbService
    ) {
        this.form = this._formBuilder.group({
            checkboxes: new FormArray([]),
        });

        this.currentPageable = Pageable.of(0, 20, undefined, undefined, Order.of('DESC', 'version'));

        this.productType = this._route.snapshot.data.productType;
        this.form = this._formBuilder.group({
            checkboxes: new FormArray([]),
        });
        this.page = (obs: Observable<Pageable>) => {
            return combineLatest([this._route.paramMap, obs]).pipe(
                tap(() => this._blockPage.block()),
                switchMap(([params, pageable]) => {
                    return this._productService.revisions(params.get('id') as string, pageable, this.range).pipe(
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
            .pipe(switchMap((params) => this._productService.show(params.get('id') as string)))
            .subscribe((product) => {
                this._product = product;
                this._breadcrumbService.set(
                    '@revision',
                    this._i8n.instant('products.revision.title', {
                        productNumber: product.productNumber,
                    })
                );
            });

        this._route.paramMap
            .pipe(
                switchMap((params) => this._productService.latestRevision(params.get('id') as string)),
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

    public get selectedCheckbox(): AbstractControl[] {
        return this.checkboxes.controls.filter((ctrl) => ctrl.value === true);
    }

    public onCheckboxClick(event: Event) {
        const el = event.target as HTMLInputElement;
        if (!el.checked) return;
        if (this.selectedCheckbox.length >= 2) {
            event.preventDefault();
        }
    }

    public onCompare(): void {
        if (!this.latest || !this._content || !this._product) return;

        const controls = this.selectedCheckbox;
        this._blockPage.block();
        this._productService
            .compareRevision(
                this._product.productNumber,
                controls.length === 1 ? 'CURRENT' : this.getCheckedRevision(this._content, controls[0]),
                controls.length === 1
                    ? this.getCheckedRevision(this._content, controls[0])
                    : this.getCheckedRevision(this._content, controls[1])
            )
            .pipe(finalize(() => this._blockPage.release()))
            .subscribe({
                next: (diff) => {
                    this.diff = diff;
                    this.portletDiff.display();
                    this.portletDiff.fullscreenPortlet('on');
                    this._cd.detectChanges();
                },
                error: (err) => this._notifierService.error(err),
            });
    }

    public getDiffDescription(): string {
        if (!this._content || !this._product || !this.diff) {
            return '';
        }
        const controls = this.selectedCheckbox;
        const rev1 = this.getCheckedRevision(this._content, controls[0]).version;
        const rev2 = controls.length >= 2 ? this.getCheckedRevision(this._content, controls[1]).version : 'CURRENT';
        if (typeof rev2 === 'string' && rev2 === 'CURRENT') {
            // eslint-disable-next-line max-len
            return `Compare the <strong>Current</strong> version of product <code class="bg-secondary p-2">${this._product.productNumber}</code> with the version <strong>v${rev1}</strong>`;
        }
        // eslint-disable-next-line max-len
        return `Compare the version <strong>v${rev1}</strong> of product <code class="bg-secondary p-2">${this._product.productNumber}</code> with the version <strong>v${rev2}</strong>`;
    }

    public onCloseDiff(): void {
        this.diff = undefined;
        this._cd.detectChanges();
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

    public goBack(event: Event): void {
        if (event) {
            event.preventDefault();
            event.stopPropagation();
        }
        this._location.back();
    }

    private getCheckedRevision(content: ProductRevision[], control: AbstractControl): ProductRevision {
        const index = this.checkboxes.controls.indexOf(control);
        return content[index];
    }
}
