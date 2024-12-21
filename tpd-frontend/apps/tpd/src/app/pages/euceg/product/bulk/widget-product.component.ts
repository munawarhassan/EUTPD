import { Component, Input } from '@angular/core';
import { EucegService, ProductList, ProductPirStatus, ProductType } from '@devacfr/euceg';
import { fromNationalMarkets } from '@tpd/app/euceg/components/market-symbol';

@Component({
    selector: 'app-widget-product',
    templateUrl: './widget-product.component.html',
})
export class WidgetProductComponent {
    @Input()
    public productType: ProductType | undefined;

    @Input()
    public product: ProductList | undefined;

    public getCountries = fromNationalMarkets;

    constructor(public euceg: EucegService) {}

    public getClassPirStatus(status: ProductPirStatus) {
        let css;
        switch (status) {
            case 'AWAITING':
                css = 'bg-secondary';
                break;
            case 'ACTIVE':
                css = 'bg-success';
                break;
            case 'INACTIVE':
                css = 'bg-warning';
                break;
            case 'WITHDRAWN':
                css = 'bg-primary';
                break;

            default:
                css = 'bg-gray-300';
                break;
        }
        return css;
    }
}
