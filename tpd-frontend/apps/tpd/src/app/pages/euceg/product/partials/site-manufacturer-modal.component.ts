import { Component, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { EucegService, ProductionSiteAddress } from '@devacfr/euceg';
import _ from 'lodash-es';
import { BsModalRef } from 'ngx-bootstrap/modal';

@Component({
    selector: 'app-site-manufacturer-modal',
    templateUrl: './site-manufacturer-modal.component.html',
})
export class SiteManufacturerModalComponent implements OnInit {
    @Output()
    public closeModal: (value: ProductionSiteAddress) => void = _.noop;

    @Output()
    public cancel: () => void = _.noop;

    public address: ProductionSiteAddress | undefined;

    public readonly = false;

    public isNew = false;

    public formControl: FormGroup;

    constructor(public bsModalRef: BsModalRef, public euceg: EucegService, private _fb: FormBuilder) {
        this.formControl = this.createForm();
    }

    ngOnInit(): void {
        this.setValue(this.address);
    }

    public onClose(): void {
        if (this.formControl.invalid) {
            return;
        }
        this.bsModalRef.hide();
        this.closeModal(this.formControl.value);
    }

    public onCancel(): void {
        this.bsModalRef.hide();
        this.cancel();
    }

    private createForm(): FormGroup {
        return this._fb.group({
            confidential: [null, [Validators.required]],
            submitterID: [null],
            Address: [null, [Validators.required]],
            Country: [null, [Validators.required]],
            PhoneNumber: [null],
            Email: [null, [Validators.email]],
        });
    }

    private setValue(address: ProductionSiteAddress | undefined) {
        if (address) {
            this.formControl.patchValue(address);
        }
        if (this.readonly) {
            this.formControl.disable();
        } else {
            this.formControl.enable();
        }
    }
}
