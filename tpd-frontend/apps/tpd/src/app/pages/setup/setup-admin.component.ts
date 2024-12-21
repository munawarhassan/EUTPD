import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { CreateAdmin, SetupService } from '@devacfr/core';
import { NotifierService, WizardStepComponent } from '@devacfr/layout';
import { lastValueFrom, of, Subscription } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';

@Component({
    selector: 'app-setup-admin',
    templateUrl: './setup-admin.component.html',
})
export class CreateAdminComponent implements OnInit, OnDestroy {
    public form: FormGroup;

    @Input()
    public step: WizardStepComponent | undefined;

    @Input()
    public data: Record<string, unknown> | undefined;

    private _subscription = new Subscription();

    constructor(
        private _formBuilder: FormBuilder,
        private _setupService: SetupService,
        private _notifierService: NotifierService
    ) {
        this.form = CreateAdmin.createFormGroup(this._formBuilder);
    }

    ngOnInit(): void {
        if (this.step) {
            this.step.beforeNext = () => {
                if (this.form.invalid) return lastValueFrom(of(false));
                if (this.data) {
                    this.data.admin = this.form.value;
                }
                return this.createAdminUser();
                // return of(true).toPromise();
            };
        }
    }

    public async createAdminUser(): Promise<boolean> {
        return await lastValueFrom(
            this._setupService.createAdmin(this.form.value).pipe(
                switchMap(() => of(true)),
                catchError((err) => {
                    this._notifierService.error(err);
                    return of(false);
                })
            )
        );
    }

    ngOnDestroy(): void {
        this._subscription.unsubscribe();
    }

    public get login() {
        return this.form.get('login');
    }
    public get password() {
        return this.form.get('password');
    }
    public get email() {
        return this.form.get('email');
    }
}
