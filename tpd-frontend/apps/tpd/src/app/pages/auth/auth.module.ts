import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DirectivesModule, InlineSVGModule } from '@devacfr/bootstrap';
import { EqualToModule, FormControlModule, PasswordInputModule } from '@devacfr/forms';
import { TranslateModule } from '@ngx-translate/core';
import { FooterModule } from '@tpd/app/partials/footer/footer.module';
import { SelectLanguageModule } from '@tpd/app/components/select-language';
import { AuthNoticeService } from './auth-notice.service';
import { AuthNoticeComponent } from './auth-notice/auth-notice.component';
import { AuthComponent } from './auth.component';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { LoginComponent } from './login/login.component';
import { LogoutComponent } from './logout/logout.component';
import { RegisterComponent } from './register/register.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: AuthComponent,
                children: [
                    {
                        path: 'login',
                        component: LoginComponent,
                    },
                    {
                        path: 'logout',
                        component: LogoutComponent,
                    },
                    {
                        path: 'register',
                        component: RegisterComponent,
                    },
                    {
                        path: 'forgot-password',
                        component: ForgotPasswordComponent,
                    },
                    {
                        path: 'reset-password',
                        component: ResetPasswordComponent,
                    },
                ],
            },
        ]),
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        DirectivesModule,
        FormControlModule,
        TranslateModule,
        InlineSVGModule,
        SelectLanguageModule,
        PasswordInputModule,
        EqualToModule,
        FooterModule,
    ],
    exports: [],
    declarations: [
        AuthComponent,
        LoginComponent,
        LogoutComponent,
        AuthNoticeComponent,
        RegisterComponent,
        ForgotPasswordComponent,
        ResetPasswordComponent,
    ],
    providers: [AuthNoticeService],
})
export class AuthViewModule {}
