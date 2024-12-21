import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminAuthGuard, AuthenticateGuard } from '@devacfr/auth';
import { BreadcrumbObject, mSvgIcons } from '@devacfr/bootstrap';
import { AuditsService } from '@devacfr/core';
import { LayoutModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { AuditComponentModule } from '@tpd/app/components/audit';
import { AuditsComponent } from './audits.component';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: AuditsComponent,
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                data: {
                    breadcrumb: {
                        label: 'audits.title',
                        icon: mSvgIcons.Simple.tools.angleGrinder,
                    } as BreadcrumbObject,
                },
            },
        ]),
        // external
        TranslateModule,
        FormsModule,
        ReactiveFormsModule,
        CommonModule,
        FormsModule,
        // internal
        LayoutModule,
        AuditComponentModule,
    ],
    exports: [],
    declarations: [AuditsComponent],
    providers: [AuditsService],
})
export class AuditsModule {}
