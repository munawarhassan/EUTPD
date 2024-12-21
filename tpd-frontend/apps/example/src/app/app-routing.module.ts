import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ExampleViewComponent } from './pages/example-view/example-view.component';

const routes: Routes = [
    {
        path: '',
        component: ExampleViewComponent,
        children: [
            {
                path: 'dashboard',
                loadChildren: () => import('./pages/dashboard/dashboard.module').then((m) => m.DashboardModule),
            },
            {
                path: 'apps/chat',
                loadChildren: () => import('./pages/apps/chat/chat.module').then((m) => m.ChatModule),
            },
            {
                path: 'apps/invoices',
                loadChildren: () => import('./pages/apps/invoices/invoice.module').then((m) => m.InvoiceModule),
            },
            {
                path: 'apps/layout',
                loadChildren: () =>
                    import('./pages/apps/layout/layout-example.module').then((m) => m.LayoutExampleModule),
            },
            {
                path: 'crafted',
                loadChildren: () => import('./pages/crafted/crafted.module').then((m) => m.CraftedModule),
            },
            {
                path: '',
                redirectTo: '/dashboard',
                pathMatch: 'full',
            },
        ],
    },
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule],
})
export class AppRoutingModule {}
