import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminAuthGuard, AuthenticateGuard } from '@devacfr/auth';
import { BreadcrumbObject, DirectivesModule, mSvgIcons } from '@devacfr/bootstrap';
import { LayoutModule, PortletModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { ServerComponent } from './server.component';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: ServerComponent,
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                data: {
                    breadcrumb: {
                        title: 'Server Settings',
                        label: 'server.title',
                        icon: mSvgIcons.Simple.devices.server,
                    } as BreadcrumbObject,
                },
            },
        ]),
        TranslateModule,
        CommonModule,
        FormsModule,
        LayoutModule,
        DirectivesModule,
        PortletModule,
    ],
    declarations: [ServerComponent],
    exports: [],
    providers: [],
})
export class AdminServerModule {}
