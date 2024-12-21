import { ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { EucegService, Manufacturer, ProductionSiteAddress, ProductRequest } from '@devacfr/euceg';
import { DataRow } from '@devacfr/util';
import { BsModalService } from 'ngx-bootstrap/modal';
import { SiteManufacturerModalComponent } from './site-manufacturer-modal.component';

@Component({
    selector: 'app-product-manufacturer',
    templateUrl: './product-manufacturer.component.html',
})
export class ProductManufacturerComponent implements OnInit {
    @Input()
    public readonly;

    @Input()
    public product: ProductRequest | undefined;

    public get selected(): Manufacturer | undefined {
        return this.dataRows.manufacturer.selected;
    }

    @Input()
    public set selected(item: Manufacturer | undefined) {
        this.dataRows.manufacturer.selected = item;
        this.dataRows.address.sync();
    }

    public dataRows = {
        manufacturer: new DataRow<Manufacturer>(this, 'product.product.Manufacturers.Manufacturer', false),
        address: new DataRow<ProductionSiteAddress>(
            this,
            'dataRows.manufacturer.selected.ProductionSiteAddresses.ProductionSiteAddress'
        ),
        sync() {
            this.manufacturer.sync();
            this.address.sync();
        },
    };

    // limit number for page links in pager
    public maxSize = 5;

    constructor(
        public svgIcons: SvgIcons,
        public euceg: EucegService,
        private _modalService: BsModalService,
        private _cd: ChangeDetectorRef
    ) {}

    ngOnInit() {
        this.dataRows.sync();
    }

    public trackAddress(index: number, address: ProductionSiteAddress): unknown {
        return address;
    }

    public removeManufacturer(manufacturer: Manufacturer) {
        this.dataRows.manufacturer.remove(manufacturer);
        this.dataRows.address.sync();
    }

    public addAddressManufacturer() {
        this.openAddressModal(undefined, true);
    }

    public updateAddressManufacturer(address: ProductionSiteAddress) {
        this.dataRows.address.selected = address;
        this.openAddressModal(this.dataRows.address.selected);
    }

    public removeAddressManufacturer(address) {
        this.dataRows.address.remove(address);
    }

    public addManufacturer() {
        this.dataRows.manufacturer.add();
    }

    public openAddressModal(address: ProductionSiteAddress | undefined, isNew = false) {
        const context = {
            address,
            readonly: this.readonly,
            isNew,
        };
        const modalRef = this._modalService.show(SiteManufacturerModalComponent, {
            animated: true,
            backdrop: true,
            initialState: context,
            class: 'modal-lg modal-dialog-centered',
        });
        const modal = modalRef.content;
        if (!modal) {
            return;
        }
        modal.closeModal = (value: ProductionSiteAddress) => {
            if (isNew) {
                this.dataRows.address.add(value);
            }
            this.dataRows.address.update();
            this._cd.detectChanges();
        };
    }
}
