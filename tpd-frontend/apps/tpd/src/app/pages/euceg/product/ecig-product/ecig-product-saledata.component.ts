import { ChangeDetectorRef, Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { EcigAnnualSalesData, EcigPresentation, EucegService } from '@devacfr/euceg';
import { DataRow } from '@devacfr/util';
import { BsModalService } from 'ngx-bootstrap/modal';
import { EcigProductSaledataModalComponent } from './ecig-product-saledata-modal.component';

@Component({
    selector: 'app-ecig-product-saledata',
    templateUrl: './ecig-product-saledata.component.html',
})
export class EcigProductSaledataComponent implements OnChanges {
    @Input()
    public readonly;

    @Input()
    public presentation: EcigPresentation | undefined;

    // limit number for page links in pager
    public maxSize = 5;

    public dataRows = {
        saledata: new DataRow<EcigAnnualSalesData>(this, 'presentation.AnnualSalesDataList.AnnualSalesData', true),
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

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.presentation.currentValue) {
            this.dataRows.sync();
        }
    }

    public addSaledata(): void {
        this.openSaledataModal(undefined, true);
    }

    public updateSaledata(sale: EcigAnnualSalesData): void {
        this.dataRows.saledata.selected = sale;
        this.openSaledataModal(this.dataRows.saledata.selected);
    }

    public removeSaledata(saledata): void {
        this.dataRows.saledata.remove(saledata);
        this._cd.markForCheck();
    }

    public trackedByYear(index, sale: EcigAnnualSalesData): string {
        return sale.Year?.value ?? '';
    }

    private openSaledataModal(saledata: EcigAnnualSalesData | undefined, isNew = false): void {
        const context = {
            saledata,
            readonly: this.readonly,
            isNew,
        };
        const modalRef = this._modalService.show(EcigProductSaledataModalComponent, {
            animated: true,
            backdrop: true,
            initialState: context,
            class: 'modal-lg modal-dialog-centered',
        });
        const modal = modalRef.content;
        if (modal) {
            modal.validate = (value: EcigAnnualSalesData) => {
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
            modal.closeModal = (value: EcigAnnualSalesData) => {
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
