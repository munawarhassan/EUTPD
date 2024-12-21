import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AccordionModule, DirectivesModule, InlineSVGModule } from '@devacfr/bootstrap';
import { Select2Module } from '@devacfr/forms';
import { WizardModule } from '@devacfr/layout';
import { WizardExampleStep1Component } from './wizard-example-step1.component';
import { WizardExampleStep2Component } from './wizard-example-step2.component';
import { WizardExampleStep3Component } from './wizard-example-step3.component';
import { WizardExampleStep4Component } from './wizard-example-step4.component';
import { WizardExampleComponent } from './wizard-example.component';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule.forChild([
            {
                path: '',
                component: WizardExampleComponent,
            },
        ]),
        WizardModule,
        AccordionModule,
        DirectivesModule,
        InlineSVGModule,
        Select2Module,
    ],
    exports: [RouterModule],
    declarations: [
        WizardExampleComponent,
        WizardExampleStep1Component,
        WizardExampleStep2Component,
        WizardExampleStep3Component,
        WizardExampleStep4Component,
    ],
    providers: [],
})
export class WizardExampleModule {}
