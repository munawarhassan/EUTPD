import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthenticateGuard } from '@devacfr/auth';
import { BreadcrumbObject } from '@devacfr/bootstrap';
import { LayoutViewComponent } from './views/layout-view.component';
import { LayoutViewModule } from './views/layout-view.module';

const viewsRoutes: Routes = [
    {
        path: '',
        component: LayoutViewComponent,
        canLoad: [AuthenticateGuard.canLoad],
        canActivate: [AuthenticateGuard.canActivate],
        children: [
            {
                path: '',
                redirectTo: '/home',
                pathMatch: 'full',
            },
            {
                path: 'account',
                redirectTo: '/account/profile',
                pathMatch: 'full',
            },
            {
                path: 'account',
                loadChildren: () => import('./account/account.module').then((m) => m.AccountViewModule),
                canLoad: [AuthenticateGuard.canLoad],
                canActivate: [AuthenticateGuard.canActivate],
            },
            {
                path: 'admin',
                loadChildren: () => import('./admin/admin.module').then((m) => m.AdminViewModule),
                canLoad: [AuthenticateGuard.canLoad],
                canActivate: [AuthenticateGuard.canActivate],
                data: {
                    breadcrumb: {
                        label: 'Administration',
                    } as BreadcrumbObject,
                },
            },
        ],
    },

    {
        path: 'home',
        loadChildren: () => import('./home/home.module').then((m) => m.HomeViewModule),
        canLoad: [AuthenticateGuard.canLoad],
        canActivate: [AuthenticateGuard.canActivate],
    },
    {
        path: 'product',
        loadChildren: () => import('./euceg/euceg.module').then((m) => m.EucegModule),
        canLoad: [AuthenticateGuard.canLoad],
        data: {
            breadcrumb: {
                label: 'Home',
            } as BreadcrumbObject,
        },
    },
    {
        path: '',
        loadChildren: () => import('./auth/auth.module').then((m) => m.AuthViewModule),
    },
    {
        path: '404',
        loadChildren: () => import('./not-found/not-found.module').then((m) => m.NotFoundViewModule),
    },
    {
        path: 'unavailable',
        loadChildren: () => import('./unavailable/unavailable.module').then((m) => m.UnavailableViewModule),
    },
    {
        path: 'setup',
        loadChildren: () => import('./setup/setup.module').then((m) => m.SetupViewModule),
    },
    {
        path: 'startup',
        loadChildren: () => import('./startup/startup.module').then((m) => m.StartupViewModule),
    },
];

@NgModule({
    imports: [LayoutViewModule, RouterModule.forChild(viewsRoutes)],
    exports: [RouterModule],
})
export class PagesRoutingModule {}
