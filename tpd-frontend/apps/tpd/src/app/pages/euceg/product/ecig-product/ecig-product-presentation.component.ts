import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { EcigPresentation, Euceg, EucegService, ProductRequest } from '@devacfr/euceg';
import { DataRow } from '@devacfr/util';
import { fromNationalMarket } from '@tpd/app/euceg/components/market-symbol';
import { Observable } from 'rxjs';

@Component({
    selector: 'app-ecig-product-presentation',
    templateUrl: './ecig-product-presentation.component.html',
})
export class EcigProductPresentationComponent implements OnChanges {
    public getCountry = fromNationalMarket;

    @Input()
    public readonly;

    @Input()
    public product: ProductRequest | undefined;

    public get selected(): EcigPresentation | undefined {
        return this.dataRows.presentation.selected;
    }

    @Input()
    public set selected(item: EcigPresentation | undefined) {
        this.dataRows.presentation.selected = item;
    }

    // limit number for page links in pager
    public maxSize = 5;

    public dataRows = {
        presentation: new DataRow<EcigPresentation>(this, 'product.product.Presentations.Presentation', false),
        sync() {
            this.presentation.sync();
        },
    };

    public nationalMarkets$: Observable<Euceg.NamedValues>;
    public productNumberTypes$: Observable<Euceg.NamedValues>;

    constructor(public svgIcons: SvgIcons, public euceg: EucegService) {
        this.nationalMarkets$ = euceg.NationalMarkets;
        this.productNumberTypes$ = euceg.ProductNumberTypes;
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.product.currentValue) {
            this.dataRows.sync();
        }
    }

    public remove(presentation: EcigPresentation) {
        this.dataRows.presentation.remove(presentation);
    }

    public add() {
        this.dataRows.presentation.add();
    }
}
