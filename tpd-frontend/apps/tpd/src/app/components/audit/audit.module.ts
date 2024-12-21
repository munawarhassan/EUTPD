import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DirectivesModule, InlineSVGModule, PipesModule } from '@devacfr/bootstrap';
import { DateRangePickerModule } from '@devacfr/forms';
import { PaginationModule, TableModule } from '@devacfr/layout';
import { AvatarUrlModule } from '../avatar/avatar.module';
import { BreadcrumbPathModule } from '../breadcrumb-path';
import { AuditDetailComponent } from './audit-detail.component';
import { AuditTimelineComponent } from './audit-timeline.component';
import { TimelineDetailsComponent } from './timeline-details.component';
import { TimelineItemComponent } from './timeline-item.component';
import { TimelineTitleComponent } from './timeline-title.component';

@NgModule({
    imports: [
        // external
        CommonModule,
        RouterModule,
        FormsModule,
        ReactiveFormsModule,
        // internal
        DirectivesModule,
        PipesModule,
        TableModule,
        PaginationModule,
        InlineSVGModule,
        DateRangePickerModule,
        BreadcrumbPathModule,
        AvatarUrlModule,
    ],
    exports: [
        AuditDetailComponent,
        AuditTimelineComponent,
        TimelineItemComponent,
        TimelineTitleComponent,
        TimelineDetailsComponent,
    ],
    declarations: [
        AuditDetailComponent,
        AuditTimelineComponent,
        TimelineItemComponent,
        TimelineTitleComponent,
        TimelineDetailsComponent,
    ],
    providers: [],
})
export class AuditComponentModule {}
