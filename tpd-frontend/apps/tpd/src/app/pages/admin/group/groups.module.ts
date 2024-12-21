import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminAuthGuard, AuthenticateGuard } from '@devacfr/auth';
import { BreadcrumbObject, DirectivesModule, InlineSVGModule, NavModule, PipesModule } from '@devacfr/bootstrap';
import { EqualToModule, FormControlModule, TagifyModule } from '@devacfr/forms';
import { LayoutModule, MenuModule, PortletModule, TableModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { ModalModule } from 'ngx-bootstrap/modal';
import { SearchModule } from '@tpd/app/components/search/search.module';
import { TagifyGroupModule } from '@tpd/app/components/tagify-group/tagify-group.module';
import { TagifyUserModule } from '@tpd/app/components/tagify-user/tagify-user.module';
import { UserCardModule } from '@tpd/app/components/user-card';
import { UserSymbolModule } from '@tpd/app/components/user-symbol';
import { AddExternalGroupModalComponent } from './add-external-group-modal.component';
import { CreateGroupModalComponent } from './create-group-modal.component';
import { AdminGroupsComponent } from './groups.component';
import { AddUserModalComponent } from './view/add-user-modal.component';
import { AdminGroupViewComponent } from './view/group-view.component';

@NgModule({
    imports: [
        // angular
        RouterModule.forChild([
            {
                path: '',
                component: AdminGroupsComponent,
                data: {
                    breadcrumb: {
                        label: 'Group Management',
                    } as BreadcrumbObject,
                },
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
            },
            {
                path: 'view/:name',
                component: AdminGroupViewComponent,
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                data: {
                    breadcrumb: {
                        alias: 'group',
                        title: 'View Group Details',
                    } as BreadcrumbObject,
                },
            },
        ]),
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        /// external
        ModalModule,
        TranslateModule,
        // internal
        LayoutModule,
        MenuModule,
        DirectivesModule,
        PipesModule,
        TableModule,
        EqualToModule,
        TagifyModule,
        PortletModule,
        SearchModule,
        InlineSVGModule,
        NavModule,
        UserCardModule,
        UserSymbolModule,
        TagifyUserModule,
        TagifyGroupModule,
        FormControlModule,
    ],
    exports: [],
    declarations: [
        AdminGroupsComponent,
        AdminGroupViewComponent,
        AddUserModalComponent,
        CreateGroupModalComponent,
        AddExternalGroupModalComponent,
    ],
})
export class AdminGroupsModule {}
