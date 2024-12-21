import { Component, HostBinding, Input } from '@angular/core';
import { ProductStatus } from '@devacfr/euceg';
import { ClassBuilder } from '@devacfr/util';

@Component({
    selector: 'app-product-status',
    templateUrl: 'product-status.component.html',
})
export class ProductStatusComponent {
    @Input()
    public size: 'lg' | 'sm' = 'lg';

    @HostBinding('class')
    public get class() {
        const builder = ClassBuilder.create('badge');
        if (this.size) builder.flag('badge-', this.size);
        if (this.status) builder.css(this.getProductClassStatus(this.status));
        return builder.toString();
    }

    @Input()
    public status: ProductStatus | undefined;

    public getProductClassStatus(status: ProductStatus) {
        let css;
        switch (status) {
            case 'DRAFT':
                css = 'badge-light-secondary';
                break;
            case 'IMPORTED':
                css = 'badge-light-warning';
                break;
            case 'SENT':
                css = 'badge-light-primary';
                break;
            case 'VALID':
                css = 'badge-light-success';
                break;

            default:
                css = 'badge-light-dark';
                break;
        }
        return css;
    }
}
