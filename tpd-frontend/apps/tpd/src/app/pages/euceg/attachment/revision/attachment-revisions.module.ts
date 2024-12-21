import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthenticateGuard, UserAuthGuard } from '@devacfr/auth';
import { BreadcrumbObject, DirectivesModule, InlineSVGModule, PipesModule } from '@devacfr/bootstrap';
import { EucegCoreModule } from '@devacfr/euceg';
import { DateRangePickerModule } from '@devacfr/forms';
import { LayoutModule, MenuModule, PortletModule, TableModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { EucegComponentModule } from '@tpd/app/euceg/components/euceg-component.module';
import { AttachmentRevisionsComponent } from './attachment-revisions.component';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule.forChild([
            {
                path: '',
                component: AttachmentRevisionsComponent,
                canActivate: [AuthenticateGuard, UserAuthGuard],
                data: {
                    breadcrumb: {
                        title: 'Attachment Revision',
                        alias: 'revision',
                    } as BreadcrumbObject,
                },
            },
        ]),
        TranslateModule,
        LayoutModule,
        DateRangePickerModule,
        DirectivesModule,
        PortletModule,
        TableModule,
        MenuModule,
        InlineSVGModule,
        EucegCoreModule,
        EucegComponentModule,
        PipesModule,
    ],
    exports: [],
    declarations: [AttachmentRevisionsComponent],
    providers: [],
})
export class AttachmentRevisionModule {}
