import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminAuthGuard, AuthenticateGuard } from '@devacfr/auth';
import { BreadcrumbObject, DirectivesModule, mSvgIcons, PipesModule } from '@devacfr/bootstrap';
import { LayoutModule, PortletModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { TrackerComponent } from './tracker.component';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: TrackerComponent,
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                data: {
                    breadcrumb: {
                        label: 'tracker.title',
                        icon: mSvgIcons.Duotone.tech.tech008,
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
        PipesModule,
    ],
    exports: [],
    declarations: [TrackerComponent],
})
export class TrackerModule {}
