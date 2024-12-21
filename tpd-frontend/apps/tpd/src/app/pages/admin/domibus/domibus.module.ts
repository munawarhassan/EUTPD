import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminAuthGuard, AuthenticateGuard } from '@devacfr/auth';
import { BreadcrumbObject, DirectivesModule, mSvgIcons } from '@devacfr/bootstrap';
import { FormControlModule } from '@devacfr/forms';
import { LayoutModule, PortletModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { DomibusComponent } from './domibus.component';

@NgModule({
    imports: [
        // angular
        RouterModule.forChild([
            {
                path: '',
                component: DomibusComponent,
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                data: {
                    breadcrumb: {
                        label: 'domibus.title',
                        title: 'Domibus Server Settings',
                        icon: mSvgIcons.Simple.electric.socketEU,
                    } as BreadcrumbObject,
                },
            },
        ]),
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        /// external
        TranslateModule,
        // internal
        LayoutModule,
        PortletModule,
        DirectivesModule,
        FormControlModule,
    ],
    exports: [],
    declarations: [DomibusComponent],
    providers: [],
})
export class AdminDomibusModule {}
