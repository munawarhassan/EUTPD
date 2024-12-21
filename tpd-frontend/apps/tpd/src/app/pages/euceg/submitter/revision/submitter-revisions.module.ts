import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthenticateGuard, UserAuthGuard } from '@devacfr/auth';
import { BreadcrumbObject, DirectivesModule, InlineSVGModule } from '@devacfr/bootstrap';
import { DateRangePickerModule } from '@devacfr/forms';
import { LayoutModule, PortletModule, TableModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { DiffHtmlModule } from '@tpd/app/components/diff';
import { SubmitterRevisionsComponent } from './submitter-revisions.component';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule.forChild([
            {
                path: '',
                component: SubmitterRevisionsComponent,
                canActivate: [AuthenticateGuard, UserAuthGuard],
                data: {
                    breadcrumb: {
                        title: 'Submitter Revision',
                        alias: 'revision',
                    } as BreadcrumbObject,
                },
            },
        ]),
        TranslateModule,
        LayoutModule,
        TableModule,
        PortletModule,
        DiffHtmlModule,
        DateRangePickerModule,
        InlineSVGModule,
        DirectivesModule,
    ],
    exports: [],
    declarations: [SubmitterRevisionsComponent],
})
export class SubmitterRevisionsModule {}
