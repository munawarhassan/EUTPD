import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminAuthGuard, AuthenticateGuard } from '@devacfr/auth';
import { BreadcrumbObject, DirectivesModule, InlineSVGModule, mSvgIcons, PipesModule } from '@devacfr/bootstrap';
import { LogsService } from '@devacfr/core';
import { LayoutModule, PortletModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { ConsoleComponent } from './console.component';
import { NgScrollbarModule } from 'ngx-scrollbar';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: ConsoleComponent,
                canActivate: [AuthenticateGuard.canActivate, AdminAuthGuard.canActivate],
                data: {
                    breadcrumb: {
                        label: 'console.title',
                        icon: mSvgIcons.Simple.code.terminal,
                    } as BreadcrumbObject,
                },
            },
        ]),
        NgScrollbarModule,
        TranslateModule,
        CommonModule,
        FormsModule,
        LayoutModule,
        DirectivesModule,
        PipesModule,
        PortletModule,
        InlineSVGModule,
    ],
    exports: [],
    declarations: [ConsoleComponent],
    providers: [LogsService],
})
export class ConsoleModule {}
