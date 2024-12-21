import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminAuthGuard, AuthenticateGuard } from '@devacfr/auth';
import { BreadcrumbObject, DirectivesModule, InlineSVGModule } from '@devacfr/bootstrap';
import { FormControlModule, PasswordInputModule } from '@devacfr/forms';
import { LayoutModule, PortletModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';

import { AdminUserCreateComponent } from './user-create.component';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: AdminUserCreateComponent,
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                data: {
                    breadcrumb: {
                        label: 'users.create.title',
                        title: 'Enter user details and submit',
                    } as BreadcrumbObject,
                },
            },
        ]),
        CommonModule,
        RouterModule,
        FormsModule,
        ReactiveFormsModule,
        // external
        TranslateModule,
        // internal
        LayoutModule,
        DirectivesModule,
        InlineSVGModule,
        FormControlModule,
        PortletModule,
        PasswordInputModule,
    ],
    exports: [RouterModule],
    declarations: [AdminUserCreateComponent],
    providers: [],
})
export class UserCreateModule {}
