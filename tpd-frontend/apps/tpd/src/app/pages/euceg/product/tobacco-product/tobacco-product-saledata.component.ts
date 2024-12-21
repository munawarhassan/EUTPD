import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { EucegService, TobaccoAnnualSalesData, TobaccoPresentation } from '@devacfr/euceg';
import { DataRow } from '@devacfr/util';
import { BsModalService } from 'ngx-bootstrap/modal';
import { TobaccoProductSaledataModalComponent } from './tobacco-product-saledata-modal.component';

@Component({
    selector: 'app-tobacco-product-saledata',
    templateUrl: './tobacco-product-saledata.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TobaccoProductSaledataComponent implements OnChanges {
    @Input()
    public readonly;

    @Input()
    public presentation: TobaccoPresentation | undefined;

    // limit number for page links in pager
    public maxSize = 5;

    public dataRows = {
        saledata: new DataRow<TobaccoAnnualSalesData>(this, 'presentation.AnnualSalesDataList.AnnualSalesData'),
        sync() {
            this.saledata.sync();
        },
    };

    constructor(
        public svgIcons: SvgIcons,
        public euceg: EucegService,
        private _modalService: BsModalService,
        private _cd: ChangeDetectorRef
    ) {}

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.presentation.currentValue) {
            this.dataRows.sync();
        }
    }

    public addSaledata(): void {
        this.openSaledataModal(undefined, true);
    }

    public updateSaledata(sale: TobaccoAnnualSalesData): void {
        this.dataRows.saledata.selected = sale;
        this.openSaledataModal(this.dataRows.saledata.selected);
    }

    public removeSaledata(saledata: TobaccoAnnualSalesData): void {
        this.dataRows.saledata.remove(saledata);
    }

    public trackedByYear(index: number, sale: TobaccoAnnualSalesData): string {
        return sale.Year?.value ?? '';
    }

    private openSaledataModal(saledata: TobaccoAnnualSalesData | undefined, isNew = false): void {
        const context = {
            saledata,
            readonly: this.readonly,
            isNew,
        };
        const modalRef = this._modalService.show(TobaccoProductSaledataModalComponent, {
            animated: true,
            backdrop: true,
            initialState: context,
            class: 'modal-lg modal-dialog-centered',
        });
        const modal = modalRef.content;
        if (modal) {
            modal.validate = (value: TobaccoAnnualSalesData) => {
                const invalid =
                    isNew &&
                    this.dataRows.saledata.content.find((s) => s.Year?.value === value.Year?.value) !== undefined;
                if (invalid) {
                    return {
                        invalid,
                        reason: 'Duplicate sale data. Please provide a valid year.',
                    };
                }
                return undefined;
            };
            modal.closeModal = (value: TobaccoAnnualSalesData) => {
                if (isNew) {
                    this.dataRows.saledata.add(value);
                }
                this.dataRows.saledata.update();
                this.dataRows.saledata.sortBy((a, b) => Number(b.Year?.value) - Number(a.Year?.value));
                this._cd.detectChanges();
            };
        }
    }
}
