import { Component, Input, OnInit } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';

const communicationType = ['email', 'sms', 'phone'];
@Component({
    selector: 'app-wizard-example-step2',
    templateUrl: './wizard-example-step2.component.html',
})
export class WizardExampleStep2Component implements OnInit {
    @Input()
    public user: any | undefined;

    public formControl: FormGroup;

    constructor(private _formBuilder: FormBuilder) {
        this.formControl = this._formBuilder.group({
            account: this._formBuilder.group({
                url: [null, [Validators.required]],
                username: [null, [Validators.required]],
                password: [null, [Validators.required]],
            }),
            settings: this._formBuilder.group({
                group: [null, [Validators.required]],
                communications: this._formBuilder.array([], [Validators.required]),
            }),
        });
        this.communications.controls.push(
            this._formBuilder.control(null),
            this._formBuilder.control(null),
            this._formBuilder.control(null)
        );
    }
    ngOnInit(): void {
        const communications = communicationType.map((t) => this.user.settings.communications.indexOf(t) > -1);
        this.formControl.setValue({
            account: this.user.account,
            settings: {
                group: this.user.settings.group,
                communications,
            },
        });
    }

    public get url(): AbstractControl | null {
        return this.formControl.get('account.url');
    }

    public get username(): AbstractControl | null {
        return this.formControl.get('account.username');
    }

    public get password(): AbstractControl | null {
        return this.formControl.get('account.password');
    }

    public get communications(): FormArray {
        return this.formControl.get('settings.communications') as FormArray;
    }

    public valid() {
        return this.formControl?.valid;
    }
}
