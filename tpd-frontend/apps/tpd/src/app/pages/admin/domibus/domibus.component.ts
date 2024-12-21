import { Location } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import {
    AbstractControl,
    AsyncValidatorFn,
    FormBuilder,
    FormControl,
    FormGroup,
    ValidationErrors,
    Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { BlockUI } from '@devacfr/bootstrap';
import { ConfigService, DomibusSetting } from '@devacfr/core';
import { NotifierService } from '@devacfr/layout';
import { EMPTY, Observable, of } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, finalize, map, tap } from 'rxjs/operators';

@Component({
    selector: 'app-domibus-settings',
    templateUrl: './domibus.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DomibusComponent implements OnInit {
    public formControl: FormGroup;

    private _block = new BlockUI('#m_portlet_domibus');

    public spinner = false;

    constructor(
        private _fb: FormBuilder,
        public _cd: ChangeDetectorRef,
        private _router: Router,
        private _configService: ConfigService,
        private _notifierService: NotifierService
    ) {
        this.formControl = this.createForm(this._fb);
    }

    ngOnInit(): void {
        this._block.block();
        this._configService
            .getDomibusSetting()
            .pipe(
                finalize(() => this._block.release()),
                catchError((err) => {
                    this._notifierService.error(err);
                    return EMPTY;
                })
            )
            .subscribe((value) => {
                this.setValue(value);
            });
    }

    public get disable(): boolean {
        return !this.formControl.get('enable')?.value;
    }

    public get url(): FormControl {
        return this.formControl.get('url') as FormControl;
    }

    public goBack(): void {
        this._router.navigate(['/admin']);
    }

    public save() {
        if (this.formControl.invalid) {
            const errors = this.getFormValidationErrors();
            if (errors && Object.keys(errors).length == 1 && this.url.hasError('healthcheck')) {
                // noop
            } else {
                return;
            }
        }

        const data = this.formControl.value as DomibusSetting;
        this._block.block();
        this._configService
            .saveDomibusSetting(data)
            .pipe(finalize(() => this._block.release()))
            .subscribe({
                next: () => {
                    this._notifierService.success('The settings has been updated');
                    this.goBack();
                },
                error: (resp) => {
                    this._notifierService.error(resp);
                },
            });
    }

    private getFormValidationErrors(): ValidationErrors | null {
        let errors: ValidationErrors | null = null;
        Object.keys(this.formControl.controls).forEach((key) => {
            const ctrl = this.formControl.get(key);
            if (ctrl) {
                if (errors == null) {
                    errors = {};
                }
                const controlErrors = ctrl.errors;
                if (controlErrors != null) {
                    errors[key] = controlErrors;
                }
            }
        });
        return errors;
    }

    private healthCheckValidator(): AsyncValidatorFn {
        return (control: AbstractControl): Promise<ValidationErrors | null> | Observable<ValidationErrors | null> => {
            return this._configService.domibusHealthCheck(control.value).pipe(
                debounceTime(1000),
                distinctUntilChanged(),
                tap(() => {
                    this.spinner = true;
                    this._cd.detectChanges();
                }),
                map((isOk) => (!isOk ? { healthcheck: true } : null)),
                finalize(() => {
                    this.spinner = false;
                    this._cd.detectChanges();
                }),
                catchError(() => of(null))
            );
        };
    }

    private createForm(fb: FormBuilder): FormGroup {
        const grp = fb.group({
            enable: [null, [Validators.required]],
            connectionType: [null, [Validators.required]],
            url: fb.control(null, [Validators.required], [this.healthCheckValidator()]),
            username: [null, [Validators.required]],
            password: [null, [Validators.required]],
            tlsInsecure: [],
            jmsOptions: fb.group({
                url: [null, [Validators.required]],
                username: [null, [Validators.required]],
                password: [null, [Validators.required]],
                concurrency: [null, [Validators.required]],
                receiveTimeout: [null, [Validators.required]],
            }),
            wsOptions: fb.group({
                authorizationType: [null, [Validators.required]],
                pendingInterval: [null, [Validators.required]],
            }),
            action: [null, [Validators.required]],
            service: [null, [Validators.required]],
            serviceType: [null, [Validators.required]],
            originalSender: [null, [Validators.required]],
            finalRecipient: [null, [Validators.required]],
            partyIdType: [null, [Validators.required]],
            fromPartyId: [null, [Validators.required]],
            toPartyId: [null, [Validators.required]],
            keyPairAlias: [null, [Validators.required]],
            trustedCertificateAlias: [null, [Validators.required]],
        });

        grp.get('enable')?.valueChanges.subscribe((enable) => {
            for (const key in this.formControl.controls) {
                if (key === 'enable') {
                    continue;
                }
                const ctrl = grp.controls[key];
                if (enable) {
                    ctrl.enable({ emitEvent: false });
                } else {
                    ctrl.disable({ emitEvent: false });
                }
            }
        });
        return grp;
    }

    private setValue(value: DomibusSetting) {
        this.formControl.setValue(value);
        this._cd.detectChanges();
    }
}
