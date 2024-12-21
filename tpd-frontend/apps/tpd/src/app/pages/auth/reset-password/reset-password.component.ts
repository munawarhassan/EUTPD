import { Component } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from '@devacfr/core';
import { NotifierService } from '@devacfr/layout';

@Component({
    selector: 'app-reset-password',
    templateUrl: './reset-password.component.html',
})
export class ResetPasswordComponent {
    private _token: string | undefined;

    public formControl: FormGroup;
    constructor(
        private _fb: FormBuilder,
        private _userService: UserService,
        private _route: ActivatedRoute,
        private _router: Router,
        private _notifier: NotifierService
    ) {
        this.formControl = this._fb.group({
            password: [null, [Validators.required, Validators.minLength(5), Validators.maxLength(50)]],
            confirmPassword: [null, [Validators.required, Validators.minLength(5), Validators.maxLength(50)]],
        });
        this._route.queryParams.subscribe((p) => (this._token = p.token));
    }

    public get password(): FormControl {
        return this.formControl.get('password') as FormControl;
    }

    public get confirmPassword(): FormControl {
        return this.formControl.get('confirmPassword') as FormControl;
    }

    public resetPassword(): void {
        if (!this._token || !this.password.value) return;
        this._userService.resetPassword(this._token, this.password.value).subscribe({
            next: () => {
                this._notifier.success('Your password has been reset');
                this._router.navigate(['home']);
            },
            error: (err) => {
                this._notifier.error(err);
            },
        });
        return;
    }
}
