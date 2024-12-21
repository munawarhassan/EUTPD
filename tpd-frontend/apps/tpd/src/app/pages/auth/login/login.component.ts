import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService, Credentials } from '@devacfr/auth';
import { BlockUI } from '@devacfr/bootstrap';
import { I18nService } from '@devacfr/layout';
import { environment } from '@tpd/environments/environment';
import { finalize } from 'rxjs/operators';
import { AuthNoticeService } from '../auth-notice.service';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss'],
    encapsulation: ViewEncapsulation.None,
})
export class LoginComponent implements OnInit {
    /**  */
    public loading: boolean | undefined;

    public authenticationError = false;

    /**  */
    private returnUrl: string | undefined;

    public formControl: FormGroup;

    public signupEnable = true;

    constructor(
        private _fb: FormBuilder,
        private _router: Router,
        private _route: ActivatedRoute,
        private _authService: AuthService,
        private _authNoticeService: AuthNoticeService,
        private _i18nService: I18nService
    ) {
        this.signupEnable = environment.features.signup.enable;
        this.formControl = this._fb.group({
            username: [null, [Validators.required]],
            password: [null, [Validators.required]],
            rememberMe: [true],
        });
    }

    public ngOnInit() {
        // get return url from route parameters or default to '/'
        this.returnUrl = this._route.snapshot.queryParams['returnUrl'] || '/';
    }

    public get username(): FormControl {
        return this.formControl.get('username') as FormControl;
    }

    public get password(): FormControl {
        return this.formControl.get('password') as FormControl;
    }
    public get rememberMe(): FormControl {
        return this.formControl.get('rememberMe') as FormControl;
    }

    public showMsg(type, msg) {
        this._authNoticeService.emit(msg, type);
    }

    public login() {
        if (this.formControl.invalid) {
            return;
        }
        const credential: Credentials = this.formControl.value;
        const block = new BlockUI().block();
        this.loading = true;
        this._authService
            .authenticate(credential)
            .pipe(finalize(() => block.release()))
            .subscribe({
                next: () => {
                    this.authenticationError = false;
                    this._router.navigate([this.returnUrl]);
                },
                error: () => {
                    this.formControl.reset({ rememberMe: this.rememberMe.value });
                    this.authenticationError = true;
                    this.loading = false;
                    this.showMsg('danger', this._i18nService.instant('login.messages.error.fail'));
                },
            });
    }
}
