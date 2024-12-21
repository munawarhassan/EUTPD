import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { EucegService, ProductRequest, TobaccoEmission } from '@devacfr/euceg';
import { DataRow } from '@devacfr/util';

@Component({
    selector: 'app-tobacco-product-other-emission',
    templateUrl: './tobacco-product-other-emission.component.html',
})
export class TobaccoProductOtherEmissionComponent implements OnChanges {
    @Input()
    public readonly;

    @Input()
    public product: ProductRequest | undefined;

    public get selected(): TobaccoEmission | undefined {
        return this.dataRows.otherEmission.selected;
    }

    @Input()
    public set selected(item: TobaccoEmission | undefined) {
        this.dataRows.otherEmission.selected = item;
    }

    // limit number for page links in pager
    public maxSize = 5;

    public dataRows = {
        otherEmission: new DataRow<TobaccoEmission>(this, 'product.product.OtherEmissions.Emission', false),
        sync() {
            this.otherEmission.sync();
        },
    };

    constructor(public svgIcons: SvgIcons, public euceg: EucegService) {}

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.product && changes.product.currentValue) {
            this.dataRows.sync();
        }
    }

    public remove(otherEmission) {
        this.dataRows.otherEmission.remove(otherEmission);
    }

    public add() {
        this.dataRows.otherEmission.add();
    }
}
