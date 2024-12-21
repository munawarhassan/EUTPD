import { Component, EventEmitter, Input, Output } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { BsColor } from '@devacfr/util';
import { FilterSubmissionType } from './submission-filter.type';

@Component({
    selector: 'app-submission-filter-result',
    templateUrl: './submission-filter-result.component.html',
    styleUrls: ['./submission-filter-result.component.scss'],
})
export class SubmissionFilterResultComponent {
    @Input()
    public filters: FilterSubmissionType[] = [];

    @Output()
    public removeTag = new EventEmitter<number>();

    @Output()
    public cleared = new EventEmitter<number>();

    @Output()
    public not = new EventEmitter<number>();

    constructor(public svgIcons: SvgIcons) {}

    public getLabelClass(filter: FilterSubmissionType): string {
        if (filter.readonly) {
            return 'badge-light-white text-gray-800';
        }
        switch (filter.property) {
            case 'presentations.nationalMarket':
                return 'badge-light-primary';
            case 'type':
            case 'productType':
            case 'submissionType':
                return 'badge-light-info';
            case 'submissionStatus':
            case 'pirStatus':
            case 'latest':
                return 'badge-light-success';
            default:
                break;
        }
        return 'badge-light-dark';
    }

    public getColor(filter: FilterSubmissionType): BsColor {
        if (filter.readonly) {
            return 'gray-700';
        }
        switch (filter.property) {
            case 'presentations.nationalMarket':
                return 'primary';
            case 'type':
            case 'productType':
            case 'submissionType':
                return 'info';
            case 'submissionStatus':
            case 'pirStatus':
            case 'latest':
                return 'gray-800';
            default:
                break;
        }
        return 'dark';
    }

    public handleNotCheck(index: number, filter: FilterSubmissionType) {
        if (!filter.readonly) {
            filter.not = !filter.not;
            this.not.emit(index);
        }
    }

    public typeOf(value) {
        return typeof value;
    }
}
