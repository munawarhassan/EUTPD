import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UserAuthGuard } from '@devacfr/auth';
import { EucegCoreModule } from '@devacfr/euceg';
import { LayoutModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { LayoutViewModule } from '../views/layout-view.module';
import { EucegViewComponent } from './euceg-view.component';

const routes: Routes = [
    {
        path: '',
        component: EucegViewComponent,
        canActivate: [UserAuthGuard.canActivate],
        canLoad: [UserAuthGuard.canLoad],
        children: [
            {
                // /product go to /home page
                path: '',
                pathMatch: 'full',
                redirectTo: '/home',
            },
            {
                path: 'submitters',
                loadChildren: () => import('./submitter/submitter.module').then((m) => m.SubmitterModule),
                canActivate: [UserAuthGuard.canActivate],
                canLoad: [UserAuthGuard.canLoad],
            },
            {
                path: 'attachments',
                loadChildren: () => import('./attachment/attachment.module').then((m) => m.AttachmentModule),
                canActivate: [UserAuthGuard.canActivate],
                canLoad: [UserAuthGuard.canLoad],
            },
            {
                path: 'submissions',
                loadChildren: () => import('./submission/submission.module').then((m) => m.SubmissionModule),
                canActivate: [UserAuthGuard.canActivate],
                canLoad: [UserAuthGuard.canLoad],
            },
            {
                path: '',
                loadChildren: () => import('./product/product.module').then((m) => m.ProductModule),
                canActivate: [UserAuthGuard.canActivate],
                canLoad: [UserAuthGuard.canLoad],
            },
        ],
    },
];

@NgModule({
    imports: [
        // angular
        RouterModule.forChild(routes),
        // external
        TranslateModule,
        // internal
        LayoutModule,
        LayoutViewModule,
        EucegCoreModule,
    ],
    exports: [RouterModule],
    declarations: [EucegViewComponent],
})
export class EucegModule {}
