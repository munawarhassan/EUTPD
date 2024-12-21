import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DirectivesModule } from '@devacfr/bootstrap';
import { FormControlModule } from '@devacfr/forms';
import { PortletModule } from '@devacfr/layout';

import { DropzoneExampleComponent } from './dropzone-example.component';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule.forChild([
            {
                path: '',
                component: DropzoneExampleComponent,
            },
        ]),
        PortletModule,
        DirectivesModule,
        FormControlModule,
    ],
    exports: [],
    declarations: [DropzoneExampleComponent],
    providers: [],
})
export class DropzoneExampleModule {}
