import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { EcigEmission, EucegService, ProductRequest } from '@devacfr/euceg';
import { DataRow, objectPath, Path } from '@devacfr/util';

@Component({
    selector: 'app-ecig-product-emission',
    templateUrl: './ecig-product-emission.component.html',
})
export class EcigProductEmissionComponent implements OnChanges {
    @Input()
    public readonly;

    @Input()
    public product: ProductRequest | undefined;

    public get selected(): EcigEmission | undefined {
        return this.dataRows.emission.selected;
    }

    @Input()
    public set selected(item: EcigEmission | undefined) {
        this.dataRows.emission.selected = item;
    }

    // limit number for page links in pager
    public maxSize = 5;

    public dataRows = {
        emission: new DataRow<EcigEmission>(this, 'product.product.Emissions.Emission', false),
        sync() {
            this.emission.sync();
        },
    };

    public emissionNames$;

    constructor(public svgIcons: SvgIcons, public euceg: EucegService) {
        this.emissionNames$ = euceg.EmissionNames;
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.product.currentValue) {
            this.dataRows.sync();
        }
    }

    public remove(emission) {
        this.dataRows.emission.remove(emission);
    }

    public add() {
        this.dataRows.emission.add();
    }

    public addToArray(obj: EcigEmission, path: Path, item: any = {}): void {
        objectPath.push(obj, path, item);
    }

    public removeFromArray(obj: EcigEmission, path: Path, item: unknown) {
        objectPath.remove(obj, path, item);
    }
}
