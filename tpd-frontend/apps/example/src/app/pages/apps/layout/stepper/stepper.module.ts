import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { StepperModule } from '@devacfr/layout';
import { StepperExampleComponent } from './stepper-example.component';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule.forChild([
            {
                path: '',
                component: StepperExampleComponent,
            },
        ]),
        StepperModule,
    ],
    exports: [RouterModule],
    declarations: [StepperExampleComponent],
    providers: [],
})
export class StepperExampleModule {}
