import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';

import { ImageInputComponent } from './image-input.component';
import { DirectivesModule } from '@devacfr/bootstrap';

@NgModule({
    imports: [CommonModule, DirectivesModule],
    exports: [ImageInputComponent],
    declarations: [ImageInputComponent],
    providers: [],
})
export class ImageInputModule {}
