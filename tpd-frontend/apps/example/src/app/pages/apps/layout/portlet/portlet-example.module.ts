import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { DirectivesModule, InlineSVGModule } from '@devacfr/bootstrap';
import { Select2Module } from '@devacfr/forms';
import { PortletModule } from '@devacfr/layout';
import { PortletExampleComponent } from './portlet-example.component';

@NgModule({
    imports: [
        CommonModule,
        RouterModule.forChild([
            {
                path: '',
                component: PortletExampleComponent,
            },
        ]),
        PortletModule,
        DirectivesModule,
        Select2Module,
        InlineSVGModule,
    ],
    exports: [RouterModule],
    declarations: [PortletExampleComponent],
    providers: [],
})
export class PortletExampleModule {}
