import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DirectivesModule, InlineSVGModule } from '@devacfr/bootstrap';
import { FormControlModule } from '@devacfr/forms';

import { ControlFormExampleComponent } from './control-form-example.component';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule.forChild([
            {
                path: '',
                component: ControlFormExampleComponent,
            },
        ]),
        DirectivesModule,
        InlineSVGModule,
        FormControlModule,
    ],
    exports: [RouterModule],
    declarations: [ControlFormExampleComponent],
    providers: [],
})
export class ControlFormExampleModule {}
