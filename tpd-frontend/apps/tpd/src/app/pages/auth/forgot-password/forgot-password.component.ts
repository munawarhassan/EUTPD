import { Component, ViewEncapsulation } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { UserService } from '@devacfr/core';
import { NotifierService } from '@devacfr/layout';
import { finalize } from 'rxjs/operators';
import { AuthNoticeService } from '../auth-notice.service';
import { ForgetPassword } from './forget-password.model';

@Component({
    selector: 'app-forgot-password',
    templateUrl: './forgot-password.component.html',
    encapsulation: ViewEncapsulation.None,
})
export class ForgotPasswordComponent {
    /**  */
    public loading = false;

    public formControl: FormGroup;

    constructor(
        private _fb: FormBuilder,
        private _router: Router,
        public _authNoticeService: AuthNoticeService,
        private _userService: UserService,
        private _notifierService: NotifierService
    ) {
        this.formControl = this._fb.group({
            username: [null, [Validators.required]],
        });
    }

    public get username(): FormControl {
        return this.formControl.get('username') as FormControl;
    }

    public requestResetPassword() {
        if (this.formControl.invalid) {
            return;
        }
        const forgetPassword: ForgetPassword = this.formControl.value;
        this.loading = true;
        this._userService
            .requestResetPassword(forgetPassword.username)
            .pipe(finalize(() => (this.loading = false)))
            .subscribe({
                next: () => {
                    this._router.navigate(['/login']);
                    this.showMsg('success', 'Cool! Password recovery instruction has been sent to your email.');
                },
                error: (err) => {
                    this.formControl.reset();
                    this._notifierService.error(err);
                },
            });
    }

    public showMsg(type, msg) {
        this._authNoticeService.emit(msg, type);
    }
}
