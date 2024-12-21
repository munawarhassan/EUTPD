import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FlatPickrDirective } from './flatpickr.directive';

@NgModule({
    imports: [CommonModule, FormsModule],
    exports: [FlatPickrDirective],
    declarations: [FlatPickrDirective],
    providers: [],
})
export class FlatPickrModule {}
