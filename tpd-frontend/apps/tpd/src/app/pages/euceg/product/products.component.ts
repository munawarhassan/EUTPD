import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BlockUI, SvgIcons } from '@devacfr/bootstrap';
import { Channels } from '@devacfr/core';
import { EucegService, ProductList, ProductService, ProductType } from '@devacfr/euceg';
import { NotifierService, TableOptions } from '@devacfr/layout';
import { Pageable, PageObserver, QueryOperator } from '@devacfr/util';
import { fromNationalMarkets } from '@tpd/app/euceg/components/market-symbol';
import { FilterProductType } from '@tpd/app/euceg/components/product-filter';
import { EMPTY, Observable } from 'rxjs';
import { catchError, finalize, switchMap, tap } from 'rxjs/operators';

@Component({
    selector: 'app-products',
    templateUrl: './products.component.html',
    styleUrls: ['./products.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductsComponent {
    public channels = [Channels.PRODUCT];

    public getCountries = fromNationalMarkets;

    public readonly: boolean;

    public productType: ProductType;

    public filters: FilterProductType[] = [];

    public tableOptions: TableOptions = {
        columns: [
            {
                name: 'productNumber',
                sort: true,
                i18n: 'products.fields.productNumber',
            },
            {
                name: 'child',
                sort: true,
                class: 'd-none d-xxl-table-cell',
                i18n: 'products.fields.child',
            },
            {
                name: 'productTypeName',
                sort: true,
                class: 'd-none d-xxl-table-cell',
                i18n: 'products.fields.productType',
            },
            {
                name: 'presentations.nationalMarketName',
                sort: true,
                i18n: 'products.fields.nationalMarkets',
            },
            {
                name: 'pirStatus',
                sort: true,
                i18n: 'products.fields.pirStatus',
            },
            {
                name: 'status',
                sort: true,
                i18n: 'products.fields.status',
            },
            {
                name: 'latestSubmissionStatus',
                sort: true,
                i18n: 'products.fields.latestSubmissionStatus',
            },
            {
                name: 'lastModifiedDate',
                sort: true,
                class: 'd-none d-xl-table-cell',
                i18n: 'products.fields.lastModifiedDate',
            },
            {
                name: 'action',
                sort: false,
                i18n: 'products.fields.action',
                align: 'center',
            },
        ],
    };

    public page: PageObserver<ProductList>;
    public currentPageable: Pageable;

    private _block = new BlockUI('#m_portlet_products');

    constructor(
        public svgIcons: SvgIcons,
        public euceg: EucegService,
        private _route: ActivatedRoute,
        private _productService: ProductService,
        private _notifierService: NotifierService
    ) {
        this.readonly = this._route.snapshot.data.readOnly;
        this.productType = this._route.snapshot.data.productType;
        this.currentPageable = Pageable.of(0, 20).order().set('lastModifiedDate', 'DESC').end();
        this.page = (obs: Observable<Pageable>) => {
            return obs.pipe(
                tap(() => {
                    this._block.block();
                }),
                switchMap((pageable) => {
                    pageable.clearFilter();
                    pageable.filter().eq('productType', this.productType);
                    this.filters.forEach((filter) => {
                        pageable.filter().op(filter.property, QueryOperator.equals, filter.not, filter.filter);
                    });
                    return this._productService.page(pageable).pipe(finalize(() => this._block.release()));
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

    public refresh(): void {
        this.currentPageable = this.currentPageable.first();
    }

    public productTrack(index: number, product: ProductList): string {
        return product.productNumber;
    }

    public onFilterChanged(evt: FilterProductType[]) {
        this.filters = evt;
        this.refresh();
    }

    public search(searchTerm: string) {
        this.currentPageable.clearFilter();
        this.currentPageable.search = Pageable.buildQuery(searchTerm);
        this.refresh();
    }

    public clearFilter() {
        this.currentPageable.clearFilter();
        this.currentPageable.search = undefined;
        this.refresh();
    }
}
