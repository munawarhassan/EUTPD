import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const startRoutes: Routes = [
    {
        path: '',
        loadChildren: () => import('./pages/pages-routing.module').then((m) => m.PagesRoutingModule),
    },
    {
        path: '**',
        redirectTo: '404',
        pathMatch: 'full',
    },
];

@NgModule({
    imports: [
        RouterModule.forRoot(startRoutes, {
    useHash: true,
    enableTracing: true
}),
    ],
    exports: [RouterModule],
})
export class AppRoutingModule {}
