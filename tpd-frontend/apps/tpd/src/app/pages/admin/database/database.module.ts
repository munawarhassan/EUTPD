import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminAuthGuard, AuthenticateGuard } from '@devacfr/auth';
import { BreadcrumbObject, DirectivesModule, InlineSVGModule, mSvgIcons } from '@devacfr/bootstrap';
import { FormControlModule } from '@devacfr/forms';
import { LayoutModule, PortletModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { ModalModule } from 'ngx-bootstrap/modal';
import { MaintenanceProgressModalModule } from '../maintenance/maintenance-progress.model.module';
import { DatabaseComponent } from './database.component';
import { MigrationComponent } from './migration/migration.component';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: DatabaseComponent,
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                data: {
                    breadcrumb: {
                        label: 'database.title',
                        icon: mSvgIcons.Simple.devices.hardDrive,
                    } as BreadcrumbObject,
                },
            },
            {
                path: 'migration',
                component: MigrationComponent,
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
            },
        ]),
        TranslateModule,
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        LayoutModule,
        FormControlModule,
        DirectivesModule,
        ModalModule,
        PortletModule,
        InlineSVGModule,
        MaintenanceProgressModalModule,
    ],
    declarations: [DatabaseComponent, MigrationComponent],
})
export class AdminDatabaseModule {}
