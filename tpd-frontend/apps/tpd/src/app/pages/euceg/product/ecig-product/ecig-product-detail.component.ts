import { Component, Input } from '@angular/core';
import { EcigProduct, Euceg, EucegService, ProductRequest } from '@devacfr/euceg';
import { Observable, of } from 'rxjs';

@Component({
    selector: 'app-ecig-product-detail',
    templateUrl: './ecig-product-detail.component.html',
})
export class EcigProductDetailComponent {
    @Input()
    public readonly;

    @Input()
    public product: ProductRequest | undefined;

    public productTypes$: Observable<Euceg.NamedValues<string>>;

    constructor(public euceg: EucegService) {
        this.productTypes$ = of(euceg.getProductTypes('ECIGARETTE'));
    }

    public get ecigProduct(): EcigProduct {
        return this.product?.product as EcigProduct;
    }
}
