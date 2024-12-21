import { Location } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BlockUI, SvgIcons } from '@devacfr/bootstrap';
import {
    BulkAction,
    BulkRequest,
    CountResult,
    Euceg,
    EucegService,
    EucegStatisticService,
    ProductList,
    ProductService,
    ProductType,
    SubmissionService,
} from '@devacfr/euceg';
import { NotifierService, SelectedTableItem, TableComponent, TableOptions, WizardStepComponent } from '@devacfr/layout';
import { ElementAnimateUtil, Page, PageObserver, Pageable, QueryOperator } from '@devacfr/util';
import { FilterProductType, ProductFilterComponent } from '@tpd/app/euceg/components/product-filter';
import _ from 'lodash-es';
import { EMPTY, Observable, of } from 'rxjs';
import { catchError, finalize, map, switchMap, tap } from 'rxjs/operators';
import Swal from 'sweetalert2';

type DisableSubmissionType = {
    name: string;
    disabled: boolean;
    value: string;
};

@Component({
    selector: 'app-product-bulk',
    templateUrl: './product-bulk.component.html',
    styleUrls: ['./product-bulk.component.scss'],
})
export class ProductBulkComponent implements OnInit {
    public tableOptions: TableOptions = {
        checkedRow: true,
        pagination: 'infinite',
        scrollTrack: 'vertical',
        minThumbSize: 50,
        columns: [
            {
                name: 'product',
                sort: false,
                i18n: 'Products',
                class: 'w-100',
            },
        ],
    };

    public page: PageObserver<SelectedTableItem<ProductList>>;
    public submissionTypeCount$: Observable<CountResult> = of();
    public currentPageable: Pageable;
    public effectivePageable!: Pageable;

    private _totalProduct = 0;
    public firstStep2Time = true;
    public selectedSubmissionType = '1';
    public submissionTypes: DisableSubmissionType[] = [];
    public overrideSubmissionType = true;

    public productType: ProductType;
    public filters: FilterProductType[] = [];

    public _selectedAll = false;
    private _selectedProducts: SelectedTableItem<ProductList>[] = [];
    private _excludedProducts: SelectedTableItem<ProductList>[] = [];

    public selectedOperation: BulkAction = 'exportExcel';

    @ViewChild(ProductFilterComponent, { static: false })
    private filterComponent!: ProductFilterComponent;
    @ViewChild(TableComponent, { static: false })
    private tableComponent!: TableComponent;

    private _block = new BlockUI();
    constructor(
        public svgIcons: SvgIcons,
        private _route: ActivatedRoute,
        private _location: Location,
        public euceg: EucegService,
        private _productService: ProductService,
        private _submissionService: SubmissionService,
        private _eucegStatisticService: EucegStatisticService,
        private _notifierService: NotifierService
    ) {
        this.submissionTypes = this.euceg.SubmissionTypes.filter((type) => type.value !== '2').map((type) => ({
            name: Euceg.truncateWithEllipses(type.name, 50),
            disabled: false,
            value: type.value,
        }));
        this.productType = this._route.snapshot.data.productType;
        this.currentPageable = Pageable.of(0, 20).order().set('lastModifiedDate', 'DESC').end();
        this.page = (obs: Observable<Pageable>) => {
            return obs.pipe(
                tap(() => {
                    this._block.block();
                }),
                switchMap((pageable) => {
                    this.addFilter(pageable);
                    this.updateEffectivePaging(pageable);
                    return this._productService.page(pageable).pipe(
                        tap((page) => {
                            this._totalProduct = page.totalElements;
                        }),
                        map((page) => {
                            const p = page as Page<SelectedTableItem<ProductList>>;
                            if (this.selectedAll) {
                                p.content.forEach((product) => (product.selected = true));
                            }
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
    }

    ngOnInit(): void {
        // this.refresh();
        console.log('init');
    }

    public get selectedAll(): boolean {
        return this._selectedAll;
    }

    public set selectedAll(value: boolean) {
        this.handleSelectAll(value);
    }

    public validStep(step: WizardStepComponent): boolean {
        // choose products step
        if (step.index === 1 && this.selectedProducts) {
            return this.selectedAll || this.selectedProducts.length > 0;
        }
        return true;
    }

    public productTrack(index: number, product: SelectedTableItem<ProductList>): string {
        return product.productNumber;
    }

    public get selectedProducts(): SelectedTableItem<ProductList>[] {
        return this._selectedProducts;
    }

    public get excludedProducts(): SelectedTableItem<ProductList>[] {
        return this._excludedProducts;
    }

    public get totalProduct(): number {
        return this._totalProduct;
    }

    public get totalSelectedProduct(): number {
        if (this.selectedAll) {
            return this.totalProduct - this.excludedProducts.length;
        }
        return this.selectedProducts.length;
    }

    public handleCheckProduct(product: SelectedTableItem<ProductList>) {
        if (this.selectedAll) {
            if (product.selected) {
                _.remove(this._excludedProducts, (e) => e === product);
            } else {
                this._excludedProducts.push(product);
            }
        } else {
            if (product.selected) {
                this._selectedProducts.push(product);
            } else {
                _.remove(this._selectedProducts, (e) => e === product);
            }
        }
        this.updateEffectivePaging(this.currentPageable);
    }

    public handleStep1Activate(active: boolean) {
        if (active) {
            this.firstStep2Time = true;
            this._selectedProducts = [];
            this._excludedProducts = [];
            this._totalProduct = 0;
            this.clearFilter();
            this.currentPageable.search = undefined;
        }
    }

    public handleStep2Activate(active: boolean) {
        if (active && this.firstStep2Time) {
            this.filterComponent.clearFilters(true);
            switch (this.selectedOperation) {
                case 'exportExcel':
                    break;
                case 'sendSubmission':
                case 'createSubmission':
                    this.filterComponent.addFilterStatus('VALID', true);
                    break;
                default:
                    break;
            }
            this.refresh(this.firstStep2Time);
            this.tableComponent.update();
            this.firstStep2Time = false;
        }
    }

    public refresh(reset = true): void {
        if (reset) {
            this.currentPageable = this.currentPageable.first();
            this._selectedProducts = [];
        }
    }

    public onFilterChanged(evt: FilterProductType[]) {
        this.filters = evt;
        this.refresh();
    }

    public clearFilter() {
        this.filters = [];
        this.currentPageable.search = undefined;
    }

    public goBack(): void {
        this._location.back();
    }

    public exportToExcel(): void {
        this._block.block();
        this._productService
            .exportToExcel(this.productType, this.effectivePageable.filters)
            .pipe(finalize(() => this._block.release()))
            .subscribe({
                next: (response) => {
                    let filename = '';
                    const disposition = response.headers.get('Content-Disposition');
                    if (disposition && disposition.indexOf('attachment') !== -1) {
                        const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
                        const matches = filenameRegex.exec(disposition);
                        if (matches != null && matches[1]) filename = matches[1].replace(/['"]/g, '');
                    }
                    if (response.body) {
                        const urlBlob = URL.createObjectURL(response.body);
                        const a = document.createElement('a');
                        a.href = urlBlob;
                        a.download = filename;
                        a.click();
                        Swal.fire({
                            title: '',
                            text: 'The export has been successfully submitted!',
                            icon: 'success',
                        });
                        this.goBack();
                    }
                },
                error: (err) => this._notifierService.error(err),
            });
    }

    public sendSubmissions(action: BulkAction): void {
        const list = this.selectedProducts.map<string>((item) => item.productNumber);

        this._block.block();
        const request: BulkRequest = {
            action,
            filters: this.effectivePageable.filters,
        };
        if (this.overrideSubmissionType && this.selectedSubmissionType) {
            request.data = {
                overrideSubmissionType: this.selectedSubmissionType,
            };
        }
        this._submissionService
            .bulkSendSubmissions(this.productType, request)
            .pipe(finalize(() => this._block.release()))
            .subscribe({
                next: () => {
                    Swal.fire({
                        title: '',
                        text: 'The send submission has been successfully submitted!',
                        icon: 'success',
                    });
                    this.goBack();
                },
                error: (err) => this._notifierService.error(err),
            });
    }

    public handleWizardChange() {
        ElementAnimateUtil.scrollTop(0, 600);
    }

    public handleWizardComplete() {
        switch (this.selectedOperation) {
            case 'exportExcel':
                this.exportToExcel();
                break;
            case 'sendSubmission':
                this.sendSubmissions('sendSubmission');
                break;
            case 'createSubmission':
                this.sendSubmissions('createSubmission');
                break;
            default:
                break;
        }
    }

    public handleSelectAll(checked: boolean) {
        this._selectedAll = checked;
        this._selectedProducts = [];
        this._excludedProducts = [];
        if (this.tableComponent) {
            this.tableComponent.getAllContents<SelectedTableItem<ProductList>>().forEach((product) => {
                product.selected = this.selectedAll;
            });
        }
        this.updateEffectivePaging(this.currentPageable);
    }

    public countResultToArray(obj: Record<string, number>): { key: string; name: string; value: number }[] {
        return Object.keys(obj).map((key) => ({
            key,
            name: this.euceg.getSubmissionType(key, -1),
            value: obj[key],
        }));
    }

    private addFilter(pageable: Pageable) {
        pageable.clearFilter();
        pageable.filter().eq('productType', this.productType);
        this.filters.forEach((filter) => {
            pageable.filter().op(filter.property, QueryOperator.equals, filter.not, filter.filter);
        });
    }

    private updateEffectivePaging(pageable: Pageable) {
        this.effectivePageable = pageable.copy();
        if (this.selectedProducts.length > 0) {
            this.effectivePageable
                .filter()
                .op(
                    'productNumber',
                    QueryOperator.in,
                    false,
                    ...this.selectedProducts.map<string>((item) => item.productNumber)
                );
        }
        if (this.excludedProducts.length > 0) {
            this.effectivePageable
                .filter()
                .op(
                    'productNumber',
                    QueryOperator.notIn,
                    false,
                    ...this.excludedProducts.map<string>((item) => item.productNumber)
                );
        }
        this.submissionTypeCount$ = this._eucegStatisticService.countProductBySubmissionType(this.effectivePageable);
    }
}
