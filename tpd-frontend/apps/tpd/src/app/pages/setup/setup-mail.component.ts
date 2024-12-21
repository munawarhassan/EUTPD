import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { BlockUI } from '@devacfr/bootstrap';
import { MailSetting, SetupService } from '@devacfr/core';
import { NotifierService, WizardStepComponent } from '@devacfr/layout';
import { lastValueFrom, of } from 'rxjs';
import { catchError, finalize, switchMap } from 'rxjs/operators';

function createReceiptForm(formBuilder: FormBuilder): FormGroup {
    return formBuilder.group({
        recipient: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(100), Validators.email]],
    });
}

@Component({
    selector: 'app-setup-mail',
    templateUrl: './setup-mail.component.html',
})
export class SetupMailComponent implements OnInit {
    @Input()
    public step: WizardStepComponent | undefined;

    @Input()
    public data: Record<string, unknown> | undefined;

    public testResult: 'SUCCESS' | 'FAILED' | undefined;
    public testing: 'on' | undefined;

    public form: FormGroup;

    private _block = new BlockUI();

    constructor(
        private _formBuilder: FormBuilder,
        private _setupService: SetupService,
        private _notifierService: NotifierService
    ) {
        this.form = this.createForm(this._formBuilder);
    }

    ngOnInit(): void {
        if (this.step) {
            this.step.valid = () => this.valid();
            this.step.beforeNext = () => {
                if (this.mailForm.invalid) return lastValueFrom(of(false));
                if (this.data) {
                    this.data.mail = this.mailForm.value;
                }
                // return of(true).toPromise();
                return this.save();
            };
        }
    }

    public get mailForm(): FormGroup {
        return this.form.get('mail') as FormGroup;
    }
    public get testForm(): FormGroup {
        return this.form.get('test') as FormGroup;
    }

    public get hostname() {
        return this.form.get('mail.hostname');
    }
    public get port() {
        return this.form.get('mail.port');
    }
    public get username() {
        return this.form.get('mail.username');
    }
    public get password() {
        return this.form.get('mail.password');
    }
    public get tls() {
        return this.form.get('mail.tls');
    }
    public get emailFrom() {
        return this.form.get('mail.emailFrom');
    }

    public get recipient(): FormControl {
        return this.form.get('test.recipient') as FormControl;
    }

    public valid(): boolean {
        return this.mailForm.valid;
    }

    public test() {
        this.testResult = undefined;
        if (this.mailForm.invalid || this.testForm.invalid) {
            return;
        }

        this.testing = 'on';

        this._setupService.testMailConnection(this.mailForm.value, this.recipient.value).subscribe({
            next: () => {
                this.testing = undefined;
                this.testResult = 'SUCCESS';
            },
            error: (err) => {
                this.testResult = 'FAILED';
                this.testing = undefined;
                this._notifierService.error(err);
            },
        });
    }

    public save(): Promise<boolean> {
        this._block.block();
        return lastValueFrom(
            this._setupService.saveMailConfiguration(this.mailForm.value).pipe(
                switchMap(() => of(true)),
                catchError((err) => {
                    this._notifierService.error(err);
                    return of(false);
                }),
                finalize(() => this._block.release())
            )
        );
    }

    private createForm(fb: FormBuilder): FormGroup {
        const grp = fb.group({
            mail: MailSetting.createFormGroup(this._formBuilder),
            test: createReceiptForm(this._formBuilder),
        });
        return grp;
    }
}
