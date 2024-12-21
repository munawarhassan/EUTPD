import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { BlockUI, SvgIcons } from '@devacfr/bootstrap';
import {
    ProductList,
    ProductPirStatus,
    ProductPirStatusList,
    ProductRequest,
    ProductRevision,
    ProductService,
} from '@devacfr/euceg';
import { NotifierService } from '@devacfr/layout';
import { finalize } from 'rxjs/operators';

@Component({
    selector: 'app-pir-status',
    templateUrl: 'pir-status.component.html',
})
export class PirStatusComponent implements OnChanges {
    public productPirStatusList = ProductPirStatusList;

    @Input()
    public readonly = true;

    @Input()
    public product: ProductRequest | ProductList | ProductRevision | undefined;

    @Input()
    public value: ProductPirStatus | undefined;

    @Output()
    public changed = new EventEmitter<ProductPirStatus>();

    private _block = new BlockUI();

    constructor(
        public svgIcons: SvgIcons,
        private _productService: ProductService,
        private _notifierService: NotifierService
    ) {}

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.product) {
            this.value = changes.product.currentValue.pirStatus;
        }
    }

    public getClassPirStatus(status: ProductPirStatus | undefined) {
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

    public updatePirStatus(status: ProductPirStatus) {
        if (!this.product) {
            return;
        }
        this._block.block();
        this._productService
            .updatePirStatus(this.product.productNumber, status)
            .pipe(finalize(() => this._block.release()))
            .subscribe({
                next: () => {
                    this.changed.emit(status);
                    this._notifierService.success('The PIR Status has been successfully updated');
                },
                error: (err) => this._notifierService.error(err),
            });
    }
}
