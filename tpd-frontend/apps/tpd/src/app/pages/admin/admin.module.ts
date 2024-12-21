import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminAuthGuard, AuthenticateGuard } from '@devacfr/auth';
import { DirectivesModule } from '@devacfr/bootstrap';
import { AsideMenuModule, LayoutModule, PortletModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { AsideAdminComponent } from './aside-admin.component';

const routes: Routes = [
    {
        path: '',
        component: AsideAdminComponent,
        outlet: 'aside',
        canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
        canLoad: [AdminAuthGuard.canLoad],
    },
    {
        path: '',
        canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
        canLoad: [AdminAuthGuard.canLoad],
        children: [
            {
                path: '',
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                loadChildren: () => import('./overview/overview.module').then((m) => m.AdminOverviewModule),
            },
            {
                path: 'users',
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                loadChildren: () => import('./user/users.module').then((m) => m.AdminUsersModule),
            },
            {
                path: 'groups',
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                loadChildren: () => import('./group/groups.module').then((m) => m.AdminGroupsModule),
            },
            {
                path: 'globalpermissions',
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                loadChildren: () =>
                    import('./global-permission/globalpermission.module').then((m) => m.GlobalPermissionModule),
            },
            {
                path: 'database',
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                loadChildren: () => import('./database/database.module').then((m) => m.AdminDatabaseModule),
            },
            {
                path: 'console',
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                loadChildren: () => import('./console/console.module').then((m) => m.ConsoleModule),
            },
            {
                path: 'indexing',
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                loadChildren: () => import('./indexing/indexing.module').then((m) => m.AdminIndexingModule),
            },
            {
                path: 'mail',
                loadChildren: () => import('./mail/mail.module').then((m) => m.AdminMailModule),
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
            },
            {
                path: 'server',
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                loadChildren: () => import('./server/server.module').then((m) => m.AdminServerModule),
            },
            {
                path: 'api',
                loadChildren: () => import('./api/api.module').then((m) => m.ApiModule),
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
            },
            {
                path: 'metrics',
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                loadChildren: () => import('./metrics/metrics.module').then((m) => m.MetricsModule),
            },
            {
                path: 'health',
                loadChildren: () => import('./health/health.module').then((m) => m.HealthModule),
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
            },
            {
                path: 'tracker',
                loadChildren: () => import('./tracker/tracker.module').then((m) => m.TrackerModule),
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
            },
            {
                path: 'audits',
                loadChildren: () => import('./audits/audits.module').then((m) => m.AuditsModule),
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
            },
            {
                path: 'ldap',
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                loadChildren: () => import('./ldap/ldap.module').then((m) => m.AdminLdapModule),
            },
            {
                path: 'keystore',
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                loadChildren: () => import('./keystore/keystore.module').then((m) => m.AdminKeystoreModule),
            },
            {
                path: 'domibus',
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                loadChildren: () => import('./domibus/domibus.module').then((m) => m.AdminDomibusModule),
            },
        ],
    },
];

@NgModule({
    imports: [
        CommonModule,
        // angular
        RouterModule.forChild(routes),
        // external
        TranslateModule,
        // internal
        LayoutModule,
        PortletModule,
        AsideMenuModule,
        DirectivesModule,
    ],
    declarations: [AsideAdminComponent],
    schemas: [],
})
export class AdminViewModule {}
