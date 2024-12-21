import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { WizardStepComponent } from './wizard-step.component';
import { WizardComponent } from './wizard.component';
import { InlineSVGModule, DirectivesModule } from '@devacfr/bootstrap';

@NgModule({
    imports: [CommonModule, FormsModule, ReactiveFormsModule, DirectivesModule, InlineSVGModule],
    exports: [WizardComponent, WizardStepComponent],
    declarations: [WizardComponent, WizardStepComponent],
})
export class WizardModule {}
