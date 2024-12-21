import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { DirectivesModule, InlineSVGModule, ProgressBarModule } from '@devacfr/bootstrap';
import { SubmissionReportService } from '@devacfr/euceg';
import { MenuModule, TableModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { AvatarUrlModule } from '@tpd/app/components/avatar/avatar.module';
import { ModalModule } from 'ngx-bootstrap/modal';
import { ReportListComponent } from './report-list.component';
import { ReportProgressModalComponent } from './report-progress.modal.component';
import { ReportsModalComponent } from './reports-modal.component';
import { AuthModule } from '@devacfr/auth';

@NgModule({
    imports: [
        CommonModule,
        RouterModule,
        ModalModule,
        TranslateModule,
        AuthModule,
        ProgressBarModule,
        InlineSVGModule,
        AvatarUrlModule,
        TableModule,
        MenuModule,
        DirectivesModule,
    ],
    exports: [ReportProgressModalComponent, ReportListComponent, ReportsModalComponent],
    declarations: [ReportProgressModalComponent, ReportListComponent, ReportsModalComponent],
    providers: [SubmissionReportService],
})
export class ReportProgressModalModule {}
