import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { DirectivesModule, InlineSVGModule } from '@devacfr/bootstrap';
import { AsideMenuModule, LayoutModule } from '@devacfr/layout';
import { TopbarModule } from '../../partials/topbar/topbar.module';

import { ExampleViewComponent } from './example-view.component';

@NgModule({
    imports: [
        CommonModule,
        RouterModule,
        LayoutModule,
        DirectivesModule,
        InlineSVGModule,
        AsideMenuModule,
        TopbarModule,
    ],
    exports: [],
    declarations: [ExampleViewComponent],
    providers: [],
})
export class ExampleViewModule {}
