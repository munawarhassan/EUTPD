import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminAuthGuard, AuthenticateGuard } from '@devacfr/auth';
import { BreadcrumbObject, DirectivesModule, InlineSVGModule, mSvgIcons, NavModule } from '@devacfr/bootstrap';
import { KeystoreService } from '@devacfr/core';
import { FormControlModule } from '@devacfr/forms';
import { LayoutModule, MenuModule, PortletModule, TableModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { FileUploadModule } from 'ng2-file-upload';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { ModalModule } from 'ngx-bootstrap/modal';
import { AlertKeystoreComponent } from './alert-keystore.component';
import { ChangeAliasModalComponent } from './change-alias-modal.component';
import { ImportKeypairModalComponent } from './import-keypair-modal.component';
import { KeystoreNotificationModalComponent } from './keystore-notification-modal.component';
import { KeystoreComponent } from './keystore.component';

@NgModule({
    imports: [
        // angular
        RouterModule.forChild([
            {
                path: '',
                component: KeystoreComponent,
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                data: {
                    breadcrumb: {
                        icon: mSvgIcons.Simple.general.shieldCheck,
                    } as BreadcrumbObject,
                },
            },
        ]),
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        /// external
        TranslateModule,
        BsDropdownModule,
        ModalModule,
        FileUploadModule,
        // internal
        LayoutModule,
        PortletModule,
        InlineSVGModule,
        TableModule,
        DirectivesModule,
        FormControlModule,
        MenuModule,
        NavModule,
    ],
    exports: [],
    declarations: [
        KeystoreComponent,
        ChangeAliasModalComponent,
        ImportKeypairModalComponent,
        KeystoreNotificationModalComponent,
        AlertKeystoreComponent,
    ],
    providers: [KeystoreService],
})
export class AdminKeystoreModule {}
