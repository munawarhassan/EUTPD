import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DirectivesModule, InlineSVGModule, PipesModule } from '@devacfr/bootstrap';
import { Select2Module } from '../select2/select2.module';
import { FileUploadModule } from 'ng2-file-upload';
import { FileDropDirective } from './file-drop.directive.component';
import { FormCheckboxComponent } from './form-checkbox.component';
import { FormDropzoneComponent } from './form-dropzone.component';
import { FormFileComponent } from './form-file.component';
import { FormRadioComponent, RadioControlRegistryModule } from './form-radio.component';
import { FormSelectComponent } from './form-select.component';
import { FormTextComponent } from './form-text.component';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        DirectivesModule,
        PipesModule,
        InlineSVGModule,
        Select2Module,
        RadioControlRegistryModule,
        FileUploadModule,
    ],
    exports: [
        FormTextComponent,
        FormSelectComponent,
        FormCheckboxComponent,
        FormRadioComponent,
        FormFileComponent,
        FormDropzoneComponent,
        FileDropDirective,
    ],
    declarations: [
        FormTextComponent,
        FormSelectComponent,
        FormCheckboxComponent,
        FormRadioComponent,
        FormFileComponent,
        FormDropzoneComponent,
        FileDropDirective,
    ],
    providers: [],
})
export class FormControlModule {}
