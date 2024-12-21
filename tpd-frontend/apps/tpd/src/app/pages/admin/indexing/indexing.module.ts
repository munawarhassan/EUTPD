import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminAuthGuard, AuthenticateGuard } from '@devacfr/auth';
import { BreadcrumbObject, DirectivesModule, InlineSVGModule, mSvgIcons, ProgressBarModule } from '@devacfr/bootstrap';
import { LayoutModule, PortletModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { ModalModule } from 'ngx-bootstrap/modal';
import { MaintenanceProgressModalModule } from '../maintenance/maintenance-progress.model.module';
import { IndexingComponent } from './indexing.component';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: IndexingComponent,
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                data: {
                    breadcrumb: {
                        label: 'indexing.title',
                        title: 'Search Index',
                        icon: mSvgIcons.Duotone.abstract.abs037,
                    } as BreadcrumbObject,
                },
            },
        ]),
        TranslateModule,
        CommonModule,
        FormsModule,
        LayoutModule,
        DirectivesModule,
        ModalModule,
        MaintenanceProgressModalModule,
        ProgressBarModule,
        PortletModule,
        InlineSVGModule,
    ],
    declarations: [IndexingComponent],
    providers: [],
})
export class AdminIndexingModule {}
