import { Component, Input } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { EucegService, ProductRequest, TobaccoProduct } from '@devacfr/euceg';
import { objectPath, Path } from '@devacfr/util';

@Component({
    selector: 'app-tobacco-product-tnco',
    templateUrl: './tobacco-product-tnco.component.html',
})
export class TobaccoProductTncoComponent {
    @Input()
    public readonly;

    @Input()
    public product: ProductRequest | undefined;

    constructor(public svgIcons: SvgIcons, public euceg: EucegService) {}

    public get tobaccoProduct(): TobaccoProduct {
        return this.product?.product as TobaccoProduct;
    }

    public addToArray(obj: any, path: Path, item: any = {}): void {
        objectPath.push(obj, path, item);
    }

    public removeFromArray(obj: any, path: Path, item: any) {
        objectPath.remove(obj, path, item);
    }
}
