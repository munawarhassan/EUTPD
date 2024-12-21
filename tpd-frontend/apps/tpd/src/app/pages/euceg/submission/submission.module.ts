import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthenticateGuard, AuthModule, UserAuthGuard } from '@devacfr/auth';
import {
    BreadcrumbObject,
    DirectivesModule,
    InlineSVGModule,
    NavModule,
    PipesModule,
    ProgressBarModule,
} from '@devacfr/bootstrap';
import { EucegCoreModule } from '@devacfr/euceg';
import { LayoutModule, MenuModule, PortletModule, TableModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { AuditComponentModule } from '@tpd/app/components/audit/audit.module';
import { SearchModule } from '@tpd/app/components/search/search.module';
import { EucegComponentModule } from '@tpd/app/euceg/components/euceg-component.module';
import { MarketSymbolModule } from '@tpd/app/euceg/components/market-symbol';
import { SubmissionFilterModule } from '@tpd/app/euceg/components/submission-filter';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { SubmissionsComponent } from './submissions.component';
import { SubmissionComponent } from './view/submission.component';
import { ReportProgressModalModule } from './report/report-progress.model.module';
import { ModalModule } from 'ngx-bootstrap/modal';

@NgModule({
    imports: [
        // angular
        RouterModule.forChild([
            {
                path: '',
                component: SubmissionsComponent,
                canActivate: [AuthenticateGuard.canActivate, UserAuthGuard.canActivate],
                data: {
                    breadcrumb: {
                        title: 'Submission Manager',
                        label: 'Submission List',
                    } as BreadcrumbObject,
                },
            },
            {
                path: 'view/:id',
                component: SubmissionComponent,
                canActivate: [AuthenticateGuard.canActivate, UserAuthGuard.canActivate],
                data: {
                    breadcrumb: {
                        title: 'Submission Detail',
                        alias: 'submission',
                    } as BreadcrumbObject,
                },
            },
        ]),
        CommonModule,
        FormsModule,
        // external
        TranslateModule,
        BsDropdownModule,
        ModalModule,
        // internal
        EucegCoreModule,
        EucegComponentModule,
        AuthModule,
        PipesModule,
        LayoutModule,
        PortletModule,
        TableModule,
        DirectivesModule,
        NavModule,
        MenuModule,
        ProgressBarModule,
        MarketSymbolModule,
        InlineSVGModule,
        SearchModule,
        AuditComponentModule,
        SubmissionFilterModule,
        ReportProgressModalModule,
    ],
    exports: [],
    declarations: [SubmissionsComponent, SubmissionComponent],
    providers: [],
})
export class SubmissionModule {}
