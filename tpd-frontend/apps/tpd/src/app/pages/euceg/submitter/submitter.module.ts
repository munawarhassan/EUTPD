import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthenticateGuard, UserAuthGuard } from '@devacfr/auth';
import { BreadcrumbObject, DirectivesModule, InlineSVGModule, PipesModule } from '@devacfr/bootstrap';
import { EucegCoreModule } from '@devacfr/euceg';
import { FormControlModule } from '@devacfr/forms';
import { LayoutModule, MenuModule, PaginationModule, PortletModule, TableModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { AuditComponentModule } from '@tpd/app/components/audit/audit.module';
import { SearchModule } from '@tpd/app/components/search/search.module';
import { EucegComponentModule } from '@tpd/app/euceg/components/euceg-component.module';
import { FileUploadModule } from 'ng2-file-upload';
import { ModalModule } from 'ngx-bootstrap/modal';
import { SubmittersComponent } from './submitters.component';
import { AffiliateModalComponent } from './view/affiliate-modal.component';
import { SubmitterViewComponent } from './view/submitter-view.component';
@NgModule({
    imports: [
        // angular
        RouterModule.forChild([
            {
                path: '',
                component: SubmittersComponent,
                canActivate: [AuthenticateGuard.canActivate, UserAuthGuard.canActivate],
                data: {
                    readOnly: true,
                    breadcrumb: {
                        title: 'Submitter Manager',
                        label: 'Submitter List',
                    } as BreadcrumbObject,
                },
            },
            {
                path: 'view/:id',
                component: SubmitterViewComponent,
                canActivate: [AuthenticateGuard.canActivate, UserAuthGuard.canActivate],
                data: {
                    readOnly: true,
                    breadcrumb: {
                        title: 'Submitter Detail View',
                        alias: 'submitter',
                    } as BreadcrumbObject,
                },
            },
            {
                path: 'edit/:id',
                component: SubmitterViewComponent,
                canActivate: [AuthenticateGuard.canActivate, UserAuthGuard.canActivate],
                data: {
                    readOnly: false,
                    breadcrumb: {
                        title: 'Submitter Detail',
                        alias: 'submitter',
                    } as BreadcrumbObject,
                },
            },
            {
                path: 'rev/:id',
                loadChildren: () =>
                    import('./revision/submitter-revisions.module').then((m) => m.SubmitterRevisionsModule),
                canLoad: [AuthenticateGuard],
                canActivate: [AuthenticateGuard.canActivate, UserAuthGuard.canActivate],
                data: {
                    readOnly: false,
                },
            },
            {
                path: 'create',
                component: SubmitterViewComponent,
                canActivate: [AuthenticateGuard.canActivate, UserAuthGuard.canActivate],
                data: {
                    readOnly: false,
                    breadcrumb: {
                        title: 'Create Submitter',
                        alias: 'submitter',
                    } as BreadcrumbObject,
                },
            },
        ]),
        CommonModule,
        FormsModule,
        // external
        TranslateModule,
        FileUploadModule,
        ModalModule,
        // internal
        FormControlModule,
        EucegCoreModule,
        PipesModule,
        LayoutModule,
        PortletModule,
        TableModule,
        DirectivesModule,
        PaginationModule,
        PortletModule,
        SearchModule,
        InlineSVGModule,
        MenuModule,
        EucegComponentModule,
        AuditComponentModule,
    ],
    exports: [RouterModule],
    declarations: [SubmittersComponent, SubmitterViewComponent, AffiliateModalComponent],
    providers: [],
})
export class SubmitterModule {}
