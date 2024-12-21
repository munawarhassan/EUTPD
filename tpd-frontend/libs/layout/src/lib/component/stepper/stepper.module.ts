import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';

import { StepperComponent } from './stepper.component';

@NgModule({
    imports: [CommonModule],
    exports: [StepperComponent],
    declarations: [StepperComponent],
    providers: [],
})
export class StepperModule {}
