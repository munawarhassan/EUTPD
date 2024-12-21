import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthenticateGuard, AuthModule, UserAuthGuard } from '@devacfr/auth';
import { BreadcrumbObject, DirectivesModule, InlineSVGModule, PipesModule } from '@devacfr/bootstrap';
import { EucegCoreModule } from '@devacfr/euceg';
import { FormControlModule, Select2Module } from '@devacfr/forms';
import { LayoutModule, MenuModule, PortletModule, TableModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { AuditComponentModule } from '@tpd/app/components/audit/audit.module';
import { BreadcrumbPathModule } from '@tpd/app/components/breadcrumb-path';
import { SearchModule } from '@tpd/app/components/search/search.module';
import { EucegComponentModule } from '@tpd/app/euceg/components/euceg-component.module';
import { FileUploadModule } from 'ng2-file-upload';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { AttachmentManager } from './attachment.manager';
import { AttachmentsComponent } from './attachments.component';
import { FolderSelectModalComponent } from './folder-select-modal.component';
import { AttachmentRevisionsComponent } from './revision/attachment-revisions.component';
import { UploadModalComponent } from './upload-modal.component';
import { AttachmentComponent } from './view/attachment.component';
import { WhereUsedComponent } from './where-used.component';

@NgModule({
    imports: [
        // angular
        RouterModule.forChild([
            {
                path: '',
                component: AttachmentsComponent,
                canActivate: [AuthenticateGuard.canActivate, UserAuthGuard.canActivate],
                data: {
                    breadcrumb: {
                        title: 'Attachment Manager',
                        label: 'File Storage',
                    } as BreadcrumbObject,
                },
            },
            {
                path: 'view/:attachment',
                component: AttachmentComponent,
                canActivate: [AuthenticateGuard.canActivate, UserAuthGuard.canActivate],
                data: {
                    breadcrumb: {
                        title: 'Attachment Detail',
                        alias: 'attachment',
                    } as BreadcrumbObject,
                },
            },
            {
                path: 'rev/:attachment',
                component: AttachmentRevisionsComponent,
                loadChildren: () =>
                    import('./revision/attachment-revisions.module').then((m) => m.AttachmentRevisionModule),
                canActivate: [AuthenticateGuard.canActivate, UserAuthGuard.canActivate],
                canLoad: [AuthenticateGuard],
            },
        ]),
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        // external
        TranslateModule,
        BsDropdownModule,
        FileUploadModule,
        // internal
        EucegCoreModule,
        EucegComponentModule,
        AuthModule,
        PipesModule,
        LayoutModule,
        PortletModule,
        TableModule,
        DirectivesModule,
        PortletModule,
        SearchModule,
        InlineSVGModule,
        MenuModule,
        Select2Module,
        FormControlModule,
        AuditComponentModule,
        BreadcrumbPathModule,
    ],
    exports: [RouterModule],
    declarations: [
        AttachmentsComponent,
        AttachmentComponent,
        UploadModalComponent,
        FolderSelectModalComponent,
        WhereUsedComponent,
    ],
    providers: [AttachmentManager],
})
export class AttachmentModule {}
