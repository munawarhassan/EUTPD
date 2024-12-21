import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminAuthGuard, AuthenticateGuard } from '@devacfr/auth';
import { BreadcrumbObject, mSvgIcons } from '@devacfr/bootstrap';
import { LayoutModule, PortletModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { ApiComponent } from './api.component';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: ApiComponent,
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                data: {
                    breadcrumb: {
                        icon: mSvgIcons.Duotone.abstract.abs026,
                        title: 'api.title',
                    } as BreadcrumbObject,
                },
            },
        ]),
        TranslateModule,
        CommonModule,
        FormsModule,
        LayoutModule,
        PortletModule,
    ],
    declarations: [ApiComponent],
    exports: [],
    providers: [],
})
export class ApiModule {}
