import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminAuthGuard, AuthenticateGuard } from '@devacfr/auth';
import { AccordionModule, BreadcrumbObject, DirectivesModule, mSvgIcons } from '@devacfr/bootstrap';
import { FormControlModule } from '@devacfr/forms';
import { LayoutModule, PortletModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { LdapComponent } from './ldap.component';

@NgModule({
    imports: [
        // angular
        RouterModule.forChild([
            {
                path: '',
                component: LdapComponent,
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                data: {
                    breadcrumb: {
                        icon: mSvgIcons.Simple.communication.addressBookCard,
                        title: 'Configure LDAP User Directory',
                    } as BreadcrumbObject,
                },
            },
        ]),
        CommonModule,
        FormsModule,
        /// external
        TranslateModule,
        // internal
        LayoutModule,
        PortletModule,
        DirectivesModule,
        FormControlModule,
        AccordionModule,
    ],
    exports: [],
    declarations: [LdapComponent],
    providers: [],
})
export class AdminLdapModule {}
