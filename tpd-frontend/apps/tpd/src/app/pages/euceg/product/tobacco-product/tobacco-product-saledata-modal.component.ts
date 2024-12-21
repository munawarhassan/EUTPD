import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TobaccoAnnualSalesData } from '@devacfr/euceg';
import _ from 'lodash-es';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';

@Component({
    selector: 'app-tobacco-product-saledata-modal',
    templateUrl: './tobacco-product-saledata-modal.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TobaccoProductSaledataModalComponent implements OnInit {
    public saledata: Partial<TobaccoAnnualSalesData> | undefined;

    public reason: string | undefined;

    public readonly = false;

    public isEcig = false;

    public isNew = false;

    public formControl: FormGroup;

    public closeModal: (value: TobaccoAnnualSalesData) => void = _.noop;

    public cancel: () => void = _.noop;

    public validate: (value: TobaccoAnnualSalesData) => { invalid: boolean; reason: string } | undefined = () =>
        undefined;

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
            MaximumSalesPrice: [null],
            SalesVolume: [null, [Validators.required]],
        });
    }

    private setValue(saledata: Partial<TobaccoAnnualSalesData> | undefined) {
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
