import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
    selector: 'app-wizard-example-step3',
    templateUrl: './wizard-example-step3.component.html',
})
export class WizardExampleStep3Component implements OnInit {
    @Input()
    public user: any | undefined;

    public formControl: FormGroup;

    constructor(private _formBuilder: FormBuilder) {
        this.formControl = this._formBuilder.group({
            cardholderName: [null, [Validators.required]],
            cardNumber: [null, [Validators.required]],
            expirationMonth: [null, [Validators.required]],
            expirationYear: [null, [Validators.required]],
            cvv: [null, [Validators.required]],
            address1: [null, [Validators.required]],
            address2: [null],
            city: [null, [Validators.required]],
            state: [null, [Validators.required]],
            zip: [null, [Validators.required]],
            country: [null, [Validators.required]],
            delivery: [null, [Validators.required]],
        });
    }

    ngOnInit(): void {
        this.formControl.setValue(this.user.billing);
    }

    public valid() {
        return this.formControl?.valid;
    }
}
