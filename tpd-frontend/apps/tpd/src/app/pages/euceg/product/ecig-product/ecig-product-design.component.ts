import { Component, Input } from '@angular/core';
import { EcigProduct, Euceg, EucegService, ProductRequest } from '@devacfr/euceg';
import { Observable } from 'rxjs';

@Component({
    selector: 'app-ecig-product-design',
    templateUrl: './ecig-product-design.component.html',
})
export class EcigProductDesignComponent {
    @Input()
    public readonly;

    @Input()
    public product: ProductRequest | undefined;

    public voltageWattageAdjustables$: Observable<Euceg.NamedValues>;

    constructor(public euceg: EucegService) {
        this.voltageWattageAdjustables$ = euceg.VoltageWattageAdjustables;
    }

    public get ecigProduct(): EcigProduct {
        return this.product?.product as EcigProduct;
    }
}
