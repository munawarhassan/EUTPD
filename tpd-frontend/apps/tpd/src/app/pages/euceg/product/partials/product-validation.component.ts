import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input } from '@angular/core';
import { ScrollOptions } from '@devacfr/bootstrap';
import { ProductRequest, ProductService } from '@devacfr/euceg';
import { ErrorResponse } from '@devacfr/util';

@Component({
    selector: 'app-product-validation',
    templateUrl: './product-validation.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductValidationComponent {
    @Input()
    public product: ProductRequest | undefined;

    public validationResponse: ErrorResponse | undefined;

    public scrollOptions: ScrollOptions = {
        activate: true,
        height: 'auto',
        dependencies: '#lt_header,#lt_footer',
    };

    constructor(private _productService: ProductService, private _cd: ChangeDetectorRef) {}

    public serverValidate() {
        this._productService.validateProduct(this.product).subscribe((data) => {
            this.validationResponse = data;
            this._cd.detectChanges();
        });
    }
}
