import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AuthenticateGuard } from '@devacfr/auth';
import { DirectivesModule, InlineSVGModule } from '@devacfr/bootstrap';
import { EucegCoreModule } from '@devacfr/euceg';
import { LayoutModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { AuditComponentModule } from '@tpd/app/components/audit';
import { ChartRecentModule } from '@tpd/app/partials/chart-recent/chart-recent.module';
import { NgApexchartsModule } from 'ng-apexcharts';
import { CountUpModule } from 'ngx-countup';
import { FooterModule } from '../../partials/footer/footer.module';
import { LayoutViewModule } from '../views/layout-view.module';
import { HomeComponent } from './home.component';

@NgModule({
    imports: [
        TranslateModule,
        CommonModule,
        RouterModule.forChild([
            {
                path: '',
                component: HomeComponent,
                canActivate: [AuthenticateGuard.canActivate],
            },
        ]),
        // internal
        EucegCoreModule,
        LayoutModule,
        LayoutViewModule,
        AuditComponentModule,
        DirectivesModule,
        FooterModule,
        InlineSVGModule,
        CountUpModule,
        ChartRecentModule,
        NgApexchartsModule,
    ],
    declarations: [HomeComponent],
})
export class HomeViewModule {}
