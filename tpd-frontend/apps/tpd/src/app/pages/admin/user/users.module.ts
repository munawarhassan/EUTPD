import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminAuthGuard, AuthenticateGuard } from '@devacfr/auth';
import { BreadcrumbObject, DirectivesModule, InlineSVGModule } from '@devacfr/bootstrap';
import { LayoutModule, MenuModule, PaginationModule, PortletModule, TableModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { SearchModule } from '@tpd/app/components/search/search.module';
import { UserCardModule } from '@tpd/app/components/user-card';
import { AdminUsersComponent } from './users.component';

@NgModule({
    imports: [
        // angular
        RouterModule.forChild([
            {
                path: '',
                data: {
                    breadcrumb: {
                        label: 'users.title',
                    } as BreadcrumbObject,
                },
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                children: [
                    {
                        path: '',
                        component: AdminUsersComponent,
                        canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                    },
                    {
                        path: 'view/:username',
                        loadChildren: () => import('./view/user-view.module').then((m) => m.UserViewModule),
                        canLoad: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                    },
                    {
                        path: 'create',
                        loadChildren: () => import('./create/user.create.module').then((m) => m.UserCreateModule),
                        canLoad: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                    },
                ],
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
        PortletModule,
        TableModule,
        UserCardModule,
        SearchModule,
        MenuModule,
        PaginationModule,
    ],
    declarations: [AdminUsersComponent],
    exports: [RouterModule],
})
export class AdminUsersModule {}
