import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NotifierService } from '@devacfr/layout';

const communicationType = ['email', 'sms', 'phone'];

@Component({
    selector: 'app-control-form-example',
    templateUrl: 'control-form-example.component.html',
})
export class ControlFormExampleComponent implements OnInit {
    public user = {
        detail: {
            name: null,
            email: 'nick.stone@gmail.com',
            phone: '1-541-754-3010',
            address1: 'Headquarters 1120 N Street Sacramento 916-654-5266',
            address2: 'P.O. Box 942873 Sacramento, CA 94273-0001',
            city: 'Polo Alto',
            state: 'California',
            country: 'US',
        },
        account: {
            url: 'http://sinortech.vertoffice.com',
            username: 'nick.stone',
            password: 'qwerty',
        },
        settings: {
            group: 2,
            communications: ['email'],
        },
        billing: {
            cardholderName: 'Nick Stone',
            cardNumber: '372955886840581',
            expirationMonth: '04',
            expirationYear: '2021',
            cvv: '450',
            address1: 'Headquarters 1120 N Street Sacramento 916-654-5266',
            address2: 'P.O. Box 942873 Sacramento, CA 94273-0001',
            city: 'Polo Alto',
            state: 'California',
            zip: '34890',
            country: 'US',
            delivery: 1,
        },
    };

    public formControl: FormGroup;

    constructor(private _formBuilder: FormBuilder, private _notifier: NotifierService) {
        this.formControl = this._formBuilder.group({
            detail: this._formBuilder.group({
                name: [null, [Validators.required]],
                email: [null, [Validators.required, Validators.email]],
                phone: [null, [Validators.required]],
                address1: [null, [Validators.required]],
                address2: [null],
                city: [null, [Validators.required]],
                state: [null, [Validators.required]],
                country: [null, [Validators.required]],
            }),
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
            detail: this.user.detail,
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

    public submit() {
        if (this.formControl.invalid) {
            this._notifier.error('Form is invalid');
        } else {
            this._notifier.success('Form is valid');
        }
    }
}
