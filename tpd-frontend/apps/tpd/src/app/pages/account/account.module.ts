import { CommonModule } from '@angular/common';
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthenticateGuard } from '@devacfr/auth';
import { DirectivesModule, InlineSVGModule } from '@devacfr/bootstrap';
import { EqualToModule, FormControlModule, ImageInputModule, PasswordInputModule } from '@devacfr/forms';
import { LayoutModule, PortletModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { FooterModule } from '@tpd/app/partials/footer/footer.module';
import { ProfileCardModule } from '@tpd/app/components/profile-card';
import { SelectLanguageModule } from '@tpd/app/components/select-language';
import { AlertProfileComponent } from './alert-profile.component';
import { AsideUserComponent } from './aside-user.component';
import { ChangePasswordProfileComponent } from './change-password/change-password-profile.component';
import { ProfileComponent } from './profile/profile.component';
import { AccountSettingsComponent } from './settings/account-settings.component';
import { TopbarModule } from '@tpd/app/partials/topbar/topbar.module';

@NgModule({
    imports: [
        // angular modules
        CommonModule,
        ReactiveFormsModule,
        FormsModule,
        RouterModule.forChild([
            {
                path: '',
                component: AsideUserComponent,
                outlet: 'aside',
                canActivate: [AuthenticateGuard.canActivate],
            },
            {
                path: '',
                children: [
                    {
                        path: '',
                        redirectTo: 'profile',
                        pathMatch: 'full',
                    },
                    {
                        path: '',
                        children: [
                            {
                                path: 'profile',
                                component: ProfileComponent,
                                data: {
                                    breadcrumbs: 'Your Personnel Information',
                                },
                            },
                        ],
                    },
                    {
                        path: '',
                        children: [
                            {
                                path: 'settings',
                                component: AccountSettingsComponent,
                                data: {
                                    breadcrumbs: 'Your Settings',
                                },
                            },
                        ],
                    },
                    {
                        path: '',
                        children: [
                            {
                                path: 'change-password',
                                component: ChangePasswordProfileComponent,
                                data: {
                                    breadcrumbs: 'Change Password',
                                },
                            },
                        ],
                    },
                ],
            },
        ]),

        // external modules
        TranslateModule,

        // internal modules
        LayoutModule,
        FooterModule,
        PortletModule,
        ImageInputModule,
        InlineSVGModule,
        ProfileCardModule,
        SelectLanguageModule,
        PasswordInputModule,
        EqualToModule,
        FormControlModule,
        DirectivesModule,
        TopbarModule,
        ImageInputModule,
    ],
    declarations: [
        AsideUserComponent,
        AlertProfileComponent,
        ProfileComponent,
        AccountSettingsComponent,
        ChangePasswordProfileComponent,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class AccountViewModule {}
