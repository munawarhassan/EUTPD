import { ChangeDetectorRef, Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { Euceg, EucegService, ProductRequest, Company, TobaccoIngredient } from '@devacfr/euceg';
import { DataRow } from '@devacfr/util';
import { BsModalService } from 'ngx-bootstrap/modal';
import { Observable } from 'rxjs';
import { SupplierModalComponent } from '../partials/supplier-modal.component';

@Component({
    selector: 'app-tobacco-product-ingredient',
    templateUrl: './tobacco-product-ingredient.component.html',
})
export class TobaccoProductIngredientComponent implements OnChanges {
    @Input()
    public readonly;

    @Input()
    public product: ProductRequest | undefined;

    public get selected(): TobaccoIngredient | undefined {
        return this.dataRows.ingredient.selected;
    }

    @Input()
    public set selected(item: TobaccoIngredient | undefined) {
        this.dataRows.ingredient.selected = item;
        this.dataRows.supplier.sync();
    }

    // limit number for page links in pager
    public maxSize = 5;

    public dataRows = {
        ingredient: new DataRow<TobaccoIngredient>(this, 'product.product.TobaccoIngredients.TobaccoIngredient', false),
        supplier: new DataRow<Company>(this, 'dataRows.ingredient.selected.Suppliers.Supplier'),
        sync() {
            this.ingredient.sync();
            this.supplier.sync();
        },
    };

    public tobaccoPartTypes$: Observable<Euceg.NamedValues>;
    public tobaccoLeafTypes$: Observable<Euceg.NamedValues>;
    public tobaccoLeafCureMethods$: Observable<Euceg.NamedValues>;

    constructor(
        public svgIcons: SvgIcons,
        public euceg: EucegService,
        private _modalService: BsModalService,
        private _cd: ChangeDetectorRef
    ) {
        this.tobaccoPartTypes$ = euceg.TobaccoPartTypes;
        this.tobaccoLeafTypes$ = euceg.TobaccoLeafTypes;
        this.tobaccoLeafCureMethods$ = euceg.TobaccoLeafCureMethods;
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.product.currentValue) {
            this.dataRows.sync();
        }
    }

    public addIngredient() {
        this.dataRows.ingredient.add();
        this.dataRows.supplier.sync();
    }

    public removeIngredient(ingredient) {
        this.dataRows.ingredient.remove(ingredient);
        this.dataRows.supplier.sync();
    }

    public addSupplier() {
        this.openSupplierModal(undefined, true);
    }

    public updateSupplier(supplier: Company) {
        this.dataRows.supplier.selected = supplier;
        this.openSupplierModal(this.dataRows.supplier.selected);
    }

    public removeSupplier(supplier) {
        this.dataRows.supplier.remove(supplier);
    }

    public openSupplierModal(supplier: Company | undefined, isNew = false) {
        const context = {
            supplier,
            readonly: this.readonly,
            isNew,
        };
        const modalRef = this._modalService.show(SupplierModalComponent, {
            animated: true,
            backdrop: true,
            initialState: context,
            class: 'modal-lg modal-dialog-centered',
        });
        const modal = modalRef.content;
        if (modal) {
            modal.closeModal = (supplier) => {
                if (isNew) {
                    this.dataRows.supplier.add(supplier);
                }
                this.dataRows.supplier.update();
                this._cd.detectChanges();
            };
        }
    }
}
