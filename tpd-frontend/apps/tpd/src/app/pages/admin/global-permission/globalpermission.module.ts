import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminAuthGuard, AuthenticateGuard } from '@devacfr/auth';
import { BreadcrumbObject, DirectivesModule, InlineSVGModule, mSvgIcons, NavModule } from '@devacfr/bootstrap';
import { GlobalPermissionService } from '@devacfr/core';
import { Select2Module } from '@devacfr/forms';
import { LayoutModule, PortletModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { TagifyGroupModule } from '@tpd/app/components/tagify-group/tagify-group.module';
import { TagifyUserModule } from '@tpd/app/components/tagify-user/tagify-user.module';
import { UserCardModule } from '@tpd/app/components/user-card';
import { AdminGlobalPermissionComponent } from './globalpermission.component';

@NgModule({
    imports: [
        // angular
        RouterModule.forChild([
            {
                path: '',
                component: AdminGlobalPermissionComponent,
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                data: {
                    breadcrumb: {
                        title: 'globalpermissions.title',
                        icon: mSvgIcons.Simple.communication.shieldUser,
                    } as BreadcrumbObject,
                },
            },
        ]),
        CommonModule,
        FormsModule,
        // external
        TranslateModule,
        // internal
        LayoutModule,
        DirectivesModule,
        TagifyUserModule,
        TagifyGroupModule,
        PortletModule,
        UserCardModule,
        NavModule,
        InlineSVGModule,
        Select2Module,
    ],
    exports: [],
    declarations: [AdminGlobalPermissionComponent],
    providers: [GlobalPermissionService],
})
export class GlobalPermissionModule {}
