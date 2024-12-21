import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { BlockUI, SvgIcons } from '@devacfr/bootstrap';
import { ConfigService, MailSetting } from '@devacfr/core';
import { NotifierService } from '@devacfr/layout';
import { finalize } from 'rxjs/operators';

@Component({
    selector: 'app-mail',
    templateUrl: './mail.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MailComponent implements OnInit {
    public testResult: 'SUCCESS' | 'FAILED' | undefined;
    public testing: 'on' | undefined;

    public formControl: FormGroup;
    public testControl: FormGroup;

    private _block = new BlockUI('#m_portlet_mail');

    constructor(
        public svgIcons: SvgIcons,
        private _fb: FormBuilder,
        private _configService: ConfigService,
        private _notifierService: NotifierService,
        private _router: Router,
        private _cd: ChangeDetectorRef
    ) {
        this.formControl = this.createForm(this._fb);
        this.testControl = this._fb.group({
            recipient: [null, [Validators.required, Validators.email]],
        });
    }

    public ngOnInit(): void {
        this._block.block();
        this._configService
            .getMailSetting()
            .pipe(finalize(() => this._block.release()))
            .subscribe({ next: (data) => this.setValue(data), error: (err) => this._notifierService.error(err) });
    }

    public save() {
        if (this.formControl.invalid) {
            return;
        }
        const data = this.formControl.value as MailSetting;
        this._block.block();
        this._configService
            .saveMailSetting(data)
            .pipe(finalize(() => this._block.release()))
            .subscribe({
                next: () => {
                    this._notifierService.successWithKey('mail.notify.updated');
                    this._router.navigate(['/admin']);
                },
                error: (err) => this._notifierService.error(err),
            });
    }

    public get recipientEmail(): FormGroup {
        return this.testControl.get('recipient') as FormGroup;
    }

    public test() {
        this.testResult = undefined;
        if (this.formControl.invalid || this.testControl.invalid) {
            return;
        }
        this.testing = 'on';
        const data = this.formControl.value as MailSetting;
        const recipientEmail = this.recipientEmail.value;
        this._configService.mailTestConnection(data, recipientEmail).subscribe({
            next: () => {
                this.testing = undefined;
                this.testResult = 'SUCCESS';
                this._cd.markForCheck();
            },
            error: (err) => {
                this.testing = undefined;
                this.testResult = 'FAILED';
                this._notifierService.error(err);
                this._cd.markForCheck();
            },
        });
    }

    public goBack(): void {
        this._router.navigate(['/admin']);
    }

    private createForm(fb: FormBuilder): FormGroup {
        const grp = fb.group({
            hostname: [null, [Validators.required]],
            port: [null, [Validators.required]],
            username: [null],
            password: [null],
            tls: [null],
            emailFrom: [null, [Validators.required, Validators.email]],
        });

        return grp;
    }

    private setValue(value: MailSetting) {
        this.formControl.setValue(value);
        this._cd.detectChanges();
    }
}
