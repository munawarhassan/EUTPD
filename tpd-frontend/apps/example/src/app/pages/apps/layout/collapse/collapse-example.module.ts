import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { DirectivesModule, InlineSVGModule } from '@devacfr/bootstrap';

import { CollapseExampleComponent } from './collapse-example.component';

@NgModule({
    imports: [
        CommonModule,
        RouterModule.forChild([
            {
                path: '',
                component: CollapseExampleComponent,
            },
        ]),
        DirectivesModule,
        InlineSVGModule,
    ],
    exports: [RouterModule],
    declarations: [CollapseExampleComponent],
    providers: [],
})
export class CollapseExampleModule {}
