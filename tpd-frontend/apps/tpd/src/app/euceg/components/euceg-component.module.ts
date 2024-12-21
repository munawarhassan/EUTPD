import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { DirectivesModule, InlineSVGModule } from '@devacfr/bootstrap';
import { FlatPickrModule, FormControlModule, Select2Module } from '@devacfr/forms';
import { MenuModule } from '@devacfr/layout';
import { ModalModule } from 'ngx-bootstrap/modal';
import { CompositionComponent } from './composition.component';
import { ConfidentialCheckboxComponent } from './confidential-checkbox.component';
import { ConfidentialDateComponent } from './confidential-date.component';
import { ConfidentialInputComponent } from './confidential-input.component';
import { ConfidentialNumberComponent } from './confidential-number.component';
import { ConfidentialSelectComponent } from './confidential-select.component';
import { ConfidentialTextareaComponent } from './confidential-textarea.component';
import { ConfidentialComponent } from './confidential.component';
import { ListFilesComponent } from './list-files.component';
import { OneFileComponent } from './one-file.component';
import { PirStatusComponent } from './pir-status.component';
import { ProductStatusComponent } from './product-status.component';
import { SubmissionStatusDirective } from './submission-status.directive';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        FormControlModule,
        InlineSVGModule,
        DirectivesModule,
        Select2Module,
        ModalModule.forChild(),
        FlatPickrModule,
        MenuModule,
    ],
    exports: [
        ConfidentialComponent,
        SubmissionStatusDirective,
        PirStatusComponent,
        ProductStatusComponent,
        ConfidentialSelectComponent,
        ConfidentialInputComponent,
        ConfidentialNumberComponent,
        ConfidentialDateComponent,
        ConfidentialTextareaComponent,
        CompositionComponent,
        ConfidentialCheckboxComponent,
        OneFileComponent,
        ListFilesComponent,
    ],
    declarations: [
        ConfidentialComponent,
        SubmissionStatusDirective,
        PirStatusComponent,
        ProductStatusComponent,
        ConfidentialSelectComponent,
        ConfidentialInputComponent,
        ConfidentialNumberComponent,
        ConfidentialDateComponent,
        ConfidentialTextareaComponent,
        CompositionComponent,
        ConfidentialCheckboxComponent,
        OneFileComponent,
        ListFilesComponent,
    ],
    providers: [],
})
export class EucegComponentModule {}
