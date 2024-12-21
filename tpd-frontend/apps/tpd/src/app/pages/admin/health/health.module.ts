import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminAuthGuard, AuthenticateGuard } from '@devacfr/auth';
import { BreadcrumbObject, DirectivesModule, mSvgIcons, PipesModule } from '@devacfr/bootstrap';
import { MonitoringService } from '@devacfr/core';
import { LayoutModule, PortletModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { HealthComponent } from './health.component';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: HealthComponent,
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                data: {
                    breadcrumb: {
                        icon: mSvgIcons.Duotone.medecine.cardio,
                        title: 'health.title',
                    } as BreadcrumbObject,
                },
            },
        ]),
        TranslateModule,
        CommonModule,
        FormsModule,
        LayoutModule,
        DirectivesModule,
        PipesModule,
        PortletModule,
    ],
    exports: [],
    declarations: [HealthComponent],
    providers: [MonitoringService],
})
export class HealthModule {}
