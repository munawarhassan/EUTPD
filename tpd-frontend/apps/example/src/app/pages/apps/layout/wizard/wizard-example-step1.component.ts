import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
    selector: 'app-wizard-example-step1',
    templateUrl: './wizard-example-step1.component.html',
})
export class WizardExampleStep1Component implements OnInit {
    @Input()
    public user: any | undefined;

    public formControl: FormGroup;

    @Output()
    public changed: EventEmitter<FormGroup> = new EventEmitter<FormGroup>();

    constructor(private _formBuilder: FormBuilder) {
        this.formControl = this._formBuilder.group({
            name: [null, [Validators.required]],
            email: [null, [Validators.required]],
            phone: [null, [Validators.required]],
            address1: [null, [Validators.required]],
            address2: [null],
            city: [null, [Validators.required]],
            state: [null, [Validators.required]],
            country: [null, [Validators.required]],
        });
    }
    ngOnInit(): void {
        this.formControl.setValue(this.user.detail);
    }

    public get name(): AbstractControl | null {
        return this.formControl.get('name');
    }

    public get email(): AbstractControl | null {
        return this.formControl.get('email');
    }

    public get phone(): AbstractControl | null {
        return this.formControl.get('phone');
    }

    public valid() {
        return this.formControl?.valid;
    }
}
