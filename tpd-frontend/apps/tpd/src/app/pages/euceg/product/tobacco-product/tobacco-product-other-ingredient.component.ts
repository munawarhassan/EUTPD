import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { Euceg, EucegService, ProductRequest, TobaccoOtherIngredient } from '@devacfr/euceg';
import { DataRow, objectPath, Path } from '@devacfr/util';
import { Observable, of } from 'rxjs';

@Component({
    selector: 'app-tobacco-product-other-ingredient',
    templateUrl: './tobacco-product-other-ingredient.component.html',
})
export class TobaccoProductOtherIngredientComponent implements OnChanges {
    @Input()
    public readonly;

    @Input()
    public product: ProductRequest | undefined;

    public get selected(): TobaccoOtherIngredient | undefined {
        return this.dataRows.otherIngredient.selected;
    }

    @Input()
    public set selected(item: TobaccoOtherIngredient | undefined) {
        this.dataRows.otherIngredient.selected = item;
    }

    // limit number for page links in pager
    public maxSize = 5;

    public dataRows = {
        otherIngredient: new DataRow<TobaccoOtherIngredient>(
            this,
            'product.product.OtherIngredients.Ingredient',
            false
        ),
        sync() {
            this.otherIngredient.sync();
        },
    };

    public ingredientCategories$: Observable<Euceg.NamedValues>;
    public reachRegistrationOrNull$: Observable<Euceg.NamedValues>;
    public ingredientFunctions$: Observable<Euceg.NamedValues>;
    public toxicityStatus$: Observable<Euceg.NamedValues>;
    public toxicologicalDataAvailables$: Observable<Euceg.NamedValues>;

    constructor(public svgIcons: SvgIcons, public euceg: EucegService) {
        this.ingredientCategories$ = euceg.IngredientCategories;
        this.reachRegistrationOrNull$ = euceg.ReachRegistration;
        this.ingredientFunctions$ = euceg.IngredientFunctions;
        this.toxicityStatus$ = euceg.ToxicityStatus;
        this.toxicologicalDataAvailables$ = euceg.ToxicologicalDataAvailables;
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.product.currentValue) {
            this.dataRows.sync();
        }
    }

    public addToArray(obj: TobaccoOtherIngredient, path: Path, item: any = {}): void {
        objectPath.push(obj, path, item);
    }

    public removeFromArray(obj: TobaccoOtherIngredient, path: Path, item: unknown) {
        objectPath.remove(obj, path, item);
    }

    public remove(otherIngredient) {
        this.dataRows.otherIngredient.remove(otherIngredient);
    }

    public add() {
        this.dataRows.otherIngredient.add();
    }
}
