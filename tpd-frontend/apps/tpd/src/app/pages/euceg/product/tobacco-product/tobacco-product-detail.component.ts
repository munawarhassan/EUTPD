import { Component, Input } from '@angular/core';
import {
    Euceg,
    EucegService,
    ProductRequest,
    SmokelessSpecific,
    TobaccoCigaretteSpecific,
    TobaccoProduct,
} from '@devacfr/euceg';
import { objectPath } from '@devacfr/util';

@Component({
    selector: 'app-tobacco-product-detail',
    templateUrl: './tobacco-product-detail.component.html',
})
export class TobaccoProductDetailComponent {
    @Input()
    public readonly;

    @Input()
    public product: ProductRequest | undefined;

    public productTypes: Euceg.NamedValues<string>;

    constructor(public euceg: EucegService) {
        this.productTypes = euceg.getProductTypes('TOBACCO') as Euceg.NamedValues<string>;
    }

    public get tobaccoProduct(): TobaccoProduct {
        return this.product?.product as TobaccoProduct;
    }

    public get NovelSpecific() {
        const v = this.tobaccoProduct.NovelSpecific;
        if (!v) {
            objectPath.set(this.tobaccoProduct, 'NovelSpecific', {});
        }
        return this.tobaccoProduct.NovelSpecific;
    }

    public get RyoPipeSpecific() {
        const v = this.tobaccoProduct.RyoPipeSpecific;
        if (!v) {
            objectPath.set(this.tobaccoProduct, 'RyoPipeSpecific', {});
        }
        return this.tobaccoProduct.RyoPipeSpecific;
    }

    public get SmokelessSpecific() {
        const v = this.tobaccoProduct.SmokelessSpecific;
        if (!v) {
            objectPath.set(this.product?.product, 'SmokelessSpecific', {});
        }
        return this.tobaccoProduct.SmokelessSpecific;
    }

    public get CigaretteSpecific() {
        const v = this.tobaccoProduct.CigaretteSpecific;
        if (!v) {
            objectPath.set(this.tobaccoProduct, 'CigaretteSpecific', {});
        }
        return this.tobaccoProduct.CigaretteSpecific;
    }
}
