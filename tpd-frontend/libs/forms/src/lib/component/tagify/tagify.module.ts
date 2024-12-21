import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TagifyDirective } from './tagify.directive';

@NgModule({
    imports: [CommonModule, FormsModule],
    exports: [TagifyDirective],
    declarations: [TagifyDirective],
    providers: [],
})
export class TagifyModule {}
