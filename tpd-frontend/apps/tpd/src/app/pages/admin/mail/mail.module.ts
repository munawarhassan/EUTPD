import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminAuthGuard, AuthenticateGuard } from '@devacfr/auth';
import { BreadcrumbObject, DirectivesModule, mSvgIcons } from '@devacfr/bootstrap';
import { FormControlModule } from '@devacfr/forms';
import { LayoutModule, PortletModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { MailComponent } from './mail.component';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: MailComponent,
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                data: {
                    breadcrumb: {
                        label: 'mail.title',
                        title: 'Mail Server',
                        icon: mSvgIcons.Simple.communication.incomingBox,
                    } as BreadcrumbObject,
                },
            },
        ]),
        TranslateModule,
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        LayoutModule,
        FormControlModule,
        DirectivesModule,
        PortletModule,
    ],
    exports: [],
    declarations: [MailComponent],
    providers: [],
})
export class AdminMailModule {}
