import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { EucegService, Company } from '@devacfr/euceg';
import _ from 'lodash-es';
import { BsModalRef } from 'ngx-bootstrap/modal';

@Component({
    selector: 'app-supplier-modal',
    templateUrl: './supplier-modal.component.html',
})
export class SupplierModalComponent implements OnInit {
    public closeModal: (supplier: Company) => void = _.noop;

    public supplier: Company | undefined;

    public readonly = false;

    public isNew = false;

    public formControl: FormGroup;

    constructor(public bsModalRef: BsModalRef, public euceg: EucegService, private _fb: FormBuilder) {
        this.formControl = this.createForm();
    }

    ngOnInit(): void {
        this.setValue(this.supplier);
    }

    public onClose(): void {
        if (this.formControl.invalid) {
            return;
        }
        this.bsModalRef.hide();
        if (this.supplier) {
            this.closeModal(this.formControl.value);
        }
    }

    public onCancel(): void {
        this.bsModalRef.hide();
    }

    private createForm(): FormGroup {
        return this._fb.group({
            confidential: [null, [Validators.required]],
            Name: [null, [Validators.required]],
            Address: [null, [Validators.required]],
            Country: [null, [Validators.required]],
            // eslint-disable-next-line no-useless-escape
            PhoneNumber: [
                null,
                [Validators.required, Validators.maxLength(20), Validators.pattern('^([0-9()/+ -]*)$')],
            ],
            Email: [null, [Validators.required, Validators.email]],
        });
    }

    private setValue(supplier: Company | undefined) {
        if (supplier) {
            this.formControl.patchValue(supplier);
        }
        if (this.readonly) {
            this.formControl.disable();
        } else {
            this.formControl.enable();
        }
    }
}
