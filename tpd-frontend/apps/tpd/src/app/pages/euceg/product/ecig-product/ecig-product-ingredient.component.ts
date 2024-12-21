import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { EcigIngredient, Euceg, EucegService, ProductRequest } from '@devacfr/euceg';
import { DataRow, objectPath, Path } from '@devacfr/util';
import { Observable } from 'rxjs';

@Component({
    selector: 'app-ecig-product-ingredient',
    templateUrl: './ecig-product-ingredient.component.html',
})
export class EcigProductIngredientComponent implements OnChanges {
    @Input()
    public readonly;

    @Input()
    public product: ProductRequest | undefined;

    public get selected() {
        return this.dataRows.ingredient.selected;
    }

    @Input()
    public set selected(item) {
        this.dataRows.ingredient.selected = item;
    }

    // limit number for page links in pager
    public maxSize = 5;

    public dataRows = {
        ingredient: new DataRow<EcigIngredient>(this, 'product.product.Ingredients.Ingredient', false),
        sync() {
            this.ingredient.sync();
        },
    };

    public reachRegistration$: Observable<Euceg.NamedValues>;
    public ingredientFunctions$: Observable<Euceg.NamedValues>;
    public toxicityStatus$: Observable<Euceg.NamedValues>;
    public toxicologicalDataAvailables$: Observable<Euceg.NamedValues>;

    constructor(public svgIcons: SvgIcons, public euceg: EucegService) {
        this.reachRegistration$ = euceg.ReachRegistration;
        this.ingredientFunctions$ = euceg.IngredientFunctions;
        this.toxicityStatus$ = euceg.ToxicityStatus;
        this.toxicologicalDataAvailables$ = euceg.ToxicologicalDataAvailables;
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.product.currentValue) {
            this.dataRows.sync();
        }
    }

    public addToArray(obj: EcigIngredient, path: Path, item: any = {}): void {
        objectPath.push(obj, path, item);
    }

    public removeFromArray(obj: EcigIngredient, path: Path, item: unknown) {
        objectPath.remove(obj, path, item);
    }

    public remove(ingredient) {
        this.dataRows.ingredient.remove(ingredient);
    }

    public add() {
        this.dataRows.ingredient.add();
    }
}
