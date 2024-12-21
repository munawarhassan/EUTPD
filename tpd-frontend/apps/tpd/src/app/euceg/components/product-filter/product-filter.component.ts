import { ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { Euceg, EucegService, ProductStatus, ProductType } from '@devacfr/euceg';

import { FilterProductType } from './product-filter.type';
import $ from 'jquery';

@Component({
    selector: 'app-product-filter',
    exportAs: 'appProductFilter',
    templateUrl: './product-filter.component.html',
    styleUrls: ['product-filter.component.scss'],
})
export class ProductFilterComponent implements OnChanges {
    @Input()
    public productType: ProductType | undefined;

    @Input()
    public storageName!: string;

    @Output()
    public changed = new EventEmitter<FilterProductType[]>();

    public productTypes: Euceg.NamedValues<string> | undefined;

    public filters: FilterProductType[] = [];
    public selectedFilterField: string | undefined;
    public selectedFilterItem: string | undefined;

    constructor(public svgIcons: SvgIcons, public euceg: EucegService, private _cd: ChangeDetectorRef) {}

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.productType?.currentValue) {
            this.productTypes = this.euceg.getProductTypes(changes.productType.currentValue);
        }
        if (changes.storageName?.currentValue) {
            const value = localStorage.getItem(this.storageName);
            if (value) {
                this.filters = JSON.parse(value) || [];
                this.changed.emit(this.filters);
            }
        }
    }

    public selectFilter(filterField?: string): void {
        this.selectedFilterField = filterField;
        this._cd.markForCheck();
        setTimeout(() => {
            switch (this.selectedFilterField) {
                case 'nationalMarket':
                    $('form-select[name="countryFilter"] button').trigger('click');
                    break;
                case 'productType':
                    $('form-select[name="productTypeFilter"] button').trigger('click');
                    break;
                case 'status':
                    $('form-select[name="statusFilter"] button').trigger('click');
                    break;
                case 'pirStatus':
                    $('form-select[name="pirStatusFilter"] button').trigger('click');
                    break;
                default:
                    break;
            }
        }, 100);
    }

    public addFilter(filter: FilterProductType): void {
        this.filters.push(filter);
        if (this.storageName) {
            localStorage.setItem(this.storageName, JSON.stringify(this.filters));
        }
        this.selectedFilterItem = undefined;
        this.selectedFilterField = undefined;
        this.changed.emit(this.filters);
    }

    public clearFilters(enforce = false): void {
        if (enforce) {
            this.filters = [];
        } else if (this.filters) {
            this.filters = this.filters.filter((e) => e.readonly);
        }
        this.selectedFilterField = undefined;
        this.selectedFilterItem = undefined;
        if (this.storageName) {
            localStorage.removeItem(this.storageName);
        }
        this.changed.emit(this.filters);
    }

    public removeTag(index: number): void {
        this.filters.splice(index, 1);
        this.storeFitler();
        this.changed.emit(this.filters);
    }

    public storeFitler() {
        if (this.storageName) {
            localStorage.setItem(this.storageName, JSON.stringify(this.filters));
        }
    }

    public addFilterProductType() {
        if (!this.selectedFilterItem) {
            return;
        }
        const type = this.euceg.getProductType(this.selectedFilterItem, this.productType);
        if (!type) return;
        const filter: FilterProductType = {
            name: 'Product Type',
            value: type,
            property: 'type',
            filter: this.selectedFilterItem,
            not: false,
        };
        this.addFilter(filter);
    }

    public addFilterCountry() {
        if (!this.selectedFilterItem) {
            return;
        }
        const val = this.euceg.getCountry(this.selectedFilterItem);
        if (!val) return;
        const filter: FilterProductType = {
            name: 'National Market',
            value: val,
            property: 'presentations.nationalMarket',
            filter: this.selectedFilterItem,
        };
        this.addFilter(filter);
    }

    public addFilterStatus(productStatus?: ProductStatus, readonly = false) {
        if ((!this.selectedFilterItem || this.selectedFilterItem === '') && !productStatus) {
            return;
        }
        let status;
        if (productStatus) {
            status = productStatus;
        } else {
            status = this.selectedFilterItem as ProductStatus;
        }
        const filter: FilterProductType = {
            name: 'Status',
            value: status,
            property: 'status',
            filter: status,
            not: false,
            readonly,
        };
        this.addFilter(filter);
    }

    public addFilterPirStatus() {
        if (!this.selectedFilterItem || this.selectedFilterItem === '') {
            return;
        }
        const filter: FilterProductType = {
            name: 'PIR Status',
            value: this.selectedFilterItem,
            property: 'pirStatus',
            filter: this.selectedFilterItem,
            not: false,
        };
        this.addFilter(filter);
    }
}
