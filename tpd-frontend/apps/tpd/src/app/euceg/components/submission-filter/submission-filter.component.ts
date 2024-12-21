import { ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { Euceg, ProductPirStatusList, EucegService, ProductType, SubmissionStatus } from '@devacfr/euceg';
import { DaterangepickerType } from '@devacfr/forms';
import { QueryOperator } from '@devacfr/util';
import moment from 'moment';
import { FilterSubmissionType } from './submission-filter.type';

@Component({
    selector: 'app-submission-filter',
    exportAs: 'appSubmissionFilter',
    templateUrl: './submission-filter.component.html',
})
export class SubmissionFilterComponent implements OnChanges {
    @Input()
    public storageName = 'submission-filter-storage';

    @Input()
    public store = true;

    @Input()
    public activatedReport = false;

    public filters: FilterSubmissionType[] = [];
    public selectedFilterField: string | undefined;
    public selectedFilterItem: string | undefined;

    public get productType(): ProductType | undefined {
        const filter = this.filters.find((v) => v.property === 'type');
        return filter ? (filter.value as ProductType) : undefined;
    }

    public productTypes: Euceg.NamedValues<string> | undefined;
    public submissionTypes: Euceg.NamedValues<string>;
    public pirStatusList: Euceg.NamedValues<string>;

    @Output()
    public changed = new EventEmitter<FilterSubmissionType[]>();

    constructor(public svgIcons: SvgIcons, public euceg: EucegService, private _cd: ChangeDetectorRef) {
        this.submissionTypes = this.euceg.SubmissionTypes;
        this.pirStatusList = ProductPirStatusList.map<Euceg.NamedValue<string>>((val) => ({
            name: val,
            value: val,
        }));
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.storageName && changes.storageName.currentValue) {
            const value = localStorage.getItem(this.storageName);
            if (value) {
                this.filters = JSON.parse(value) || [];
                this.productTypes = this.euceg.getProductTypes(this.productType);
                this._cd.detectChanges();
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
                case 'product':
                    $('form-select[name="productFilter"] button').trigger('click');
                    break;
                case 'productType':
                    $('form-select[name="productTypeFilter"] button').trigger('click');
                    break;
                case 'submissionType':
                    $('form-select[name="submissionTypeFilter"] button').trigger('click');
                    break;
                case 'submissionStatus':
                    $('form-select[name="statusFilter"] button').trigger('click');
                    break;
                case 'lastModifiedDate':
                    $('form-select[name="lastModifiedDate"] button').trigger('click');
                    break;
                case 'latest':
                    this.addFilterLatestSubmission();
                    break;
                default:
                    break;
            }
        }, 100);
    }

    public addFilter(filter: FilterSubmissionType): void {
        this.filters.push(filter);
        if (this.store) {
            localStorage.setItem(this.storageName, JSON.stringify(this.filters));
        }
        this.selectedFilterItem = undefined;
        this.selectedFilterField = undefined;
        this.changed.emit(this.filters);
    }

    public clearFilters(): void {
        this.filters = [];
        this.selectedFilterField = undefined;
        this.selectedFilterItem = undefined;
        localStorage.removeItem(this.storageName);
        this.changed.emit(this.filters);
    }

    public removeTag(index: number): void {
        this.filters.splice(index, 1);
        if (this.store) {
            localStorage.setItem(this.storageName, JSON.stringify(this.filters));
        }
        this.changed.emit(this.filters);
    }

    public addFilterProductType(category?: string | string[], readonly = false, not?: boolean) {
        if (!this.selectedFilterItem && !category) {
            return;
        }
        let types: string[] | undefined;
        if (category) {
            types = typeof category === 'string' ? [category] : category;
        } else {
            types = this.selectedFilterItem ? [this.selectedFilterItem] : undefined;
        }
        if (!types) {
            return;
        }
        types.forEach((type) => {
            const typeName = this.euceg.getProductType(type, this.productType);
            if (!typeName) return;
            const filter: FilterSubmissionType = {
                name: 'Product Type',
                value: typeName,
                property: 'productType',
                filter: type,
                op: QueryOperator.equals,
                readonly,
                not: not,
            };
            this.addFilter(filter);
        });
    }

    public addFilterProduct(productType?: ProductType, readonly = false) {
        if (!this.selectedFilterItem && !productType) {
            return;
        }
        let type;
        if (productType) {
            type = productType;
        } else {
            type = this.selectedFilterItem as ProductType;
        }
        const filter: FilterSubmissionType = {
            name: 'Product',
            value: type,
            property: 'type',
            filter: type,
            op: QueryOperator.equals,
            readonly,
        };
        this.addFilter(filter);
        this.productTypes = this.euceg.getProductTypes(type);
        this._cd.detectChanges();
    }

    public addFilterType() {
        if (!this.selectedFilterItem) {
            return;
        }
        const type = this.euceg.getProductType(this.selectedFilterItem, this.productType);
        if (!type) return;
        const filter: FilterSubmissionType = {
            name: 'Type',
            value: type,
            property: 'productType',
            filter: this.selectedFilterItem,
            op: QueryOperator.equals,
        };
        this.addFilter(filter);
    }

    public addFilterLatestSubmission() {
        const filter: FilterSubmissionType = {
            name: 'Latest Submission',
            value: 'true',
            property: 'latestSubmitted',
            filter: 'true',
            op: QueryOperator.equals,
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
        const filter: FilterSubmissionType = {
            name: 'National Market',
            value: val,
            property: 'presentations.nationalMarket',
            filter: this.selectedFilterItem,
            op: QueryOperator.equals,
        };
        this.addFilter(filter);
    }

    public addFilterStatus(submissionStatus?: SubmissionStatus, readonly = false) {
        if ((!this.selectedFilterItem || this.selectedFilterItem === '') && !submissionStatus) {
            return;
        }
        let status;
        if (submissionStatus) {
            status = submissionStatus;
        } else {
            status = this.selectedFilterItem;
        }
        const filter: FilterSubmissionType = {
            name: 'Status',
            value: status,
            property: 'submissionStatus',
            filter: status,
            op: QueryOperator.equals,
            not: false,
            readonly,
        };
        this.addFilter(filter);
    }

    public addFilterSubmissionType() {
        if (!this.selectedFilterItem || this.selectedFilterItem === '') {
            return;
        }
        const filter: FilterSubmissionType = {
            name: 'Type',
            value: this.euceg.getSubmissionType(this.selectedFilterItem),
            property: 'submissionType',
            filter: this.selectedFilterItem,
            op: QueryOperator.equals,
        };
        this.addFilter(filter);
    }

    public addLastModifiedDate(range: DaterangepickerType) {
        let value = '';
        if (typeof range.startDate === 'undefined' && typeof range.endDate === 'undefined') {
            value = 'All';
        } else if (range?.label === 'Today' || range?.label === 'Yesterday') {
            value = `${this.toLocalDate(range.startDate)}`;
        } else {
            value = `${this.toLocalDate(range.startDate)}, ${this.toLocalDate(range.endDate)}`;
        }
        const filter = [range.startDate?.toISOString(), range.endDate?.toISOString()];
        const f: FilterSubmissionType = {
            name: 'Submitted',
            value,
            property: 'lastModifiedDate',
            filter,
            op: QueryOperator.between,
        };
        this.addFilter(f);
    }

    public addFilterPirStatus() {
        if (!this.selectedFilterItem || this.selectedFilterItem === '') {
            return;
        }
        const filter: FilterSubmissionType = {
            name: 'PIR Status',
            value: this.selectedFilterItem,
            property: 'pirStatus',
            filter: this.selectedFilterItem,
            op: QueryOperator.equals,
            not: false,
        };
        this.addFilter(filter);
    }

    private toLocalDate(date: Date | undefined): string | undefined {
        if (!date) {
            return;
        }
        const d = moment(date);
        return d.format('DD.MM.YYYY');
    }
}
