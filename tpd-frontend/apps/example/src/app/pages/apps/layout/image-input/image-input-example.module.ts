import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ImageInputModule } from '@devacfr/forms';

import { ImageInputExampleComponent } from './image-input-example.component';

@NgModule({
    imports: [
        CommonModule,
        RouterModule.forChild([
            {
                path: '',
                component: ImageInputExampleComponent,
            },
        ]),
        ImageInputModule,
    ],
    exports: [RouterModule],
    declarations: [ImageInputExampleComponent],
    providers: [],
})
export class ImageInputExampleModule {}
