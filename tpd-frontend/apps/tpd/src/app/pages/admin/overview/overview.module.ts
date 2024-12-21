import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AdminAuthGuard, AuthenticateGuard } from '@devacfr/auth';
import { InlineSVGModule } from '@devacfr/bootstrap';
import { PortletModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';

import { AdminOverviewComponent } from './overview.component';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: AdminOverviewComponent,
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
            },
        ]),
        TranslateModule,
        PortletModule,
        InlineSVGModule,
    ],
    declarations: [AdminOverviewComponent],
})
export class AdminOverviewModule {}
