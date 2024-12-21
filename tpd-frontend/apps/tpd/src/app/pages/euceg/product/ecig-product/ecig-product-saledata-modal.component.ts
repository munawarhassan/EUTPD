import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { EcigAnnualSalesData } from '@devacfr/euceg';
import _ from 'lodash-es';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';

@Component({
    selector: 'app-ecig-product-saledata-modal',
    templateUrl: './ecig-product-saledata-modal.component.html',
})
export class EcigProductSaledataModalComponent implements OnInit {
    public saledata: Partial<EcigAnnualSalesData> | undefined;

    public reason: string | undefined;

    public readonly = false;

    public isEcig = false;

    public isNew = false;

    public formControl: FormGroup;

    public closeModal: (value: EcigAnnualSalesData) => void = _.noop;

    public cancel: () => void = _.noop;

    public validate: (value: EcigAnnualSalesData) => { invalid: boolean; reason: string } | undefined = () => undefined;

    constructor(public _fb: FormBuilder, public bsModalRef: BsModalRef, public _modalService: BsModalService) {
        this.formControl = this.createForm();
    }

    ngOnInit(): void {
        this.setValue(this.saledata);
    }

    public onClose(): void {
        if (this.formControl.invalid) return;
        const value = this.formControl.value;
        const result = this.validate(value);
        if (result) {
            this.reason = result.reason;
            return;
        } else {
            this.bsModalRef.hide();
        }
        this.closeModal(value);
    }

    public onCancel(): void {
        this.bsModalRef.hide();
        this.cancel();
    }

    private createForm(): FormGroup {
        return this._fb.group({
            Year: [null, [Validators.required]],
            SalesMode: [null],
            SalesVolume: [null, [Validators.required]],
        });
    }

    private setValue(saledata: Partial<EcigAnnualSalesData> | undefined) {
        if (saledata) {
            this.formControl.patchValue(saledata);
        }
        if (this.readonly) {
            this.formControl.disable();
        }
        if (this.readonly || !this.isNew) {
            this.formControl.get('Year')?.disable();
        }
    }
}
