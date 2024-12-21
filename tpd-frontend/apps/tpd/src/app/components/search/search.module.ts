import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { InlineSVGModule } from '@devacfr/bootstrap';
import { SearchComponent } from './search.component';

@NgModule({
    imports: [CommonModule, FormsModule, ReactiveFormsModule, InlineSVGModule],
    exports: [SearchComponent],
    declarations: [SearchComponent],
})
export class SearchModule {}
