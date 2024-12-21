import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { DirectivesModule, ProgressBarModule } from '@devacfr/bootstrap';
import { LayoutModule } from '@devacfr/layout';

import { StartupFailedComponent } from './startup-failed.component';
import { StartupComponent } from './startup.component';

@NgModule({
    imports: [
        CommonModule,
        LayoutModule,
        DirectivesModule,
        ProgressBarModule,
        RouterModule.forChild([
            {
                path: '',
                component: StartupComponent,
            },
            {
                path: 'failed',
                component: StartupFailedComponent,
            },
        ]),
    ],
    declarations: [StartupComponent, StartupFailedComponent],
})
export class StartupViewModule {}
