import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminAuthGuard, AuthenticateGuard } from '@devacfr/auth';
import { BreadcrumbObject, DirectivesModule, InlineSVGModule, NavModule } from '@devacfr/bootstrap';
import { EqualToModule, FormControlModule, PasswordInputModule } from '@devacfr/forms';
import { LayoutModule, MenuModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { ModalModule } from 'ngx-bootstrap/modal';
import { ProfileCardModule } from '@tpd/app/components/profile-card';
import { UserCardModule } from '@tpd/app/components/user-card';
import { ChangePasswordModalComponent } from './change-password-modal.component';
import { UserEditModalComponent } from './user-edit-modal.component';
import { UserRenameModalComponent } from './user-rename-modal.component';
import { AdminUserViewComponent } from './user-view.component';

@NgModule({
    imports: [
        // angular
        RouterModule.forChild([
            {
                path: '',
                component: AdminUserViewComponent,
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                data: {
                    breadcrumb: {
                        alias: 'user',
                        title: 'View User Details',
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
        ModalModule,
        // internal
        InlineSVGModule,
        LayoutModule,
        DirectivesModule,
        PasswordInputModule,
        EqualToModule,
        FormControlModule,
        ProfileCardModule,
        UserCardModule,
        MenuModule,
        NavModule,
    ],
    exports: [RouterModule],
    declarations: [
        AdminUserViewComponent,
        UserRenameModalComponent,
        ChangePasswordModalComponent,
        UserEditModalComponent,
    ],
    providers: [],
})
export class UserViewModule {}
