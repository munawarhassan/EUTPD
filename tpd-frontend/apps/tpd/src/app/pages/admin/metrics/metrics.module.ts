import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminAuthGuard, AuthenticateGuard } from '@devacfr/auth';
import {
    AccordionModule,
    BreadcrumbObject,
    DirectivesModule,
    InlineSVGModule,
    mSvgIcons,
    PipesModule,
    ProgressBarModule,
} from '@devacfr/bootstrap';
import { LayoutModule, PortletModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { NgxGaugeModule } from 'ngx-gauge';
import { MetricsMemoryComponent } from './metrics-memory.component';
import { MetricsNetworkComponent } from './metrics-network.component';
import { MetricsProcessorComponent } from './metrics-processor.component';
import { MetricsThreadDumpComponent } from './metrics-threaddump-component';
import { MetricsThreadsComponent } from './metrics-threads.component';
import { MetricsComponent } from './metrics.component';
import { NgChartsModule } from 'ng2-charts';
import { NgScrollbar } from 'ngx-scrollbar';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: MetricsComponent,
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                data: {
                    breadcrumb: {
                        title: 'metrics.title',
                        label: 'Metrics',
                        icon: mSvgIcons.Simple.devices.diagnostics,
                    } as BreadcrumbObject,
                },
            },
        ]),
        TranslateModule,
        NgScrollbar,
        CommonModule,
        FormsModule,
        LayoutModule,
        DirectivesModule,
        PortletModule,
        PipesModule,
        NgxGaugeModule,
        NgChartsModule,
        ProgressBarModule,
        AccordionModule,
        InlineSVGModule,
    ],
    declarations: [
        MetricsComponent,
        MetricsProcessorComponent,
        MetricsMemoryComponent,
        MetricsThreadsComponent,
        MetricsNetworkComponent,
        MetricsThreadDumpComponent,
    ],
    exports: [],
    providers: [],
})
export class MetricsModule {}
