import { Component, EventEmitter, Input, Output } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { BsColor } from '@devacfr/util';
import { FilterProductType } from '.';

@Component({
    selector: 'app-product-filter-result',
    templateUrl: 'product-filter-result.component.html',
    styleUrls: ['./product-filter-result.component.scss'],
})
export class ProductFilterResultComponent {
    @Input()
    public filters: FilterProductType[] = [];

    @Output()
    public removeTag = new EventEmitter<number>();

    @Output()
    public cleared = new EventEmitter<number>();

    @Output()
    public notChanged = new EventEmitter<number>();

    constructor(public svgIcons: SvgIcons) {}

    public getLabelClass(filter: FilterProductType): string {
        if (filter.readonly) {
            return 'badge-light-white text-gray-800';
        }
        switch (filter.property) {
            case 'presentations.nationalMarket':
                return 'badge-light-primary';
            case 'productType':
                return 'badge-light-info';
            case 'status':
            case 'pirStatus':
                return 'badge-light-success';
            default:
                break;
        }
        return 'badge-light-dark';
    }

    public getColor(filter: FilterProductType): BsColor {
        if (filter.readonly) {
            return 'gray-700';
        }
        switch (filter.property) {
            case 'presentations.nationalMarket':
                return 'primary';
            case 'productType':
                return 'info';
            case 'status':
            case 'pirStatus':
                return 'success';

            default:
                break;
        }
        return 'dark';
    }

    public handleNotCheck(index: number, filter: FilterProductType) {
        if (filter.readonly) {
            return;
        }
        filter.not = !filter.not;
        this.notChanged.emit(index);
    }

    public typeOf(value) {
        return typeof value;
    }
}
