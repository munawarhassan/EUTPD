// Angular
import { Component } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { RegisterUser } from '@devacfr/auth';
import { UserService } from '@devacfr/core';
import { EqualToValidator } from '@devacfr/forms';
import { NotifierService } from '@devacfr/layout';
import { finalize } from 'rxjs/operators';
import { AuthNoticeService, NoticeType } from '../auth-notice.service';

@Component({
    selector: 'app-register',
    templateUrl: './register.component.html',
})
export class RegisterComponent {
    /**  */
    public loading: boolean | undefined;

    public formControl: FormGroup;

    constructor(
        private _fb: FormBuilder,
        private _authNoticeService: AuthNoticeService,
        private _notifierService: NotifierService,
        private _router: Router,
        private _userService: UserService
    ) {
        this.formControl = this._fb.group({
            username: [
                null,
                [
                    Validators.required,
                    Validators.minLength(3),
                    Validators.maxLength(20),
                    Validators.pattern('^[a-zA-Z0-9]*$'),
                ],
            ],
            displayName: [null, [Validators.required]],
            email: [null, [Validators.required, Validators.email]],
            password: [null, [Validators.required, Validators.minLength(5), Validators.maxLength(50)]],
            confirmPassword: [null, [Validators.required, EqualToValidator.equalTo('password')]],
        });
    }

    public get username(): FormControl {
        return this.formControl.get('username') as FormControl;
    }
    public get displayName(): FormControl {
        return this.formControl.get('displayName') as FormControl;
    }

    public get email(): FormControl {
        return this.formControl.get('email') as FormControl;
    }

    public get password(): FormControl {
        return this.formControl.get('password') as FormControl;
    }

    public get confirmPassword(): FormControl {
        return this.formControl.get('confirmPassword') as FormControl;
    }
    public register() {
        if (this.formControl.invalid) {
            return;
        }
        this.loading = true;
        const signupData = this.formControl.value as RegisterUser;
        this._userService
            .registerUser(signupData)
            .pipe(finalize(() => (this.loading = false)))
            .subscribe({
                next: () => {
                    this._router.navigate(['/login']);
                    this.showMsg('success', 'Thank you. To complete your registration please check your email.');
                },
                error: (err) => {
                    this.formControl.reset();
                    if (err.status === 400) {
                        this.showMsg('danger', err);
                    } else {
                        this._notifierService.error(err);
                    }
                },
            });
    }

    public showMsg(type: NoticeType, msg: string) {
        this._authNoticeService.emit(msg, type);
    }
}
