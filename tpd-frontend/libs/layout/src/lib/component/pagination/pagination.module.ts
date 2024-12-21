import { CommonModule } from '@angular/common';
import { ModuleWithProviders, NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PaginationComponent } from './pagination.component';
import { DirectivesModule, InlineSVGModule } from '@devacfr/bootstrap';
import { Select2Module } from '@devacfr/forms';

@NgModule({
    imports: [CommonModule, FormsModule, DirectivesModule, InlineSVGModule, Select2Module],
    declarations: [PaginationComponent],
    exports: [PaginationComponent],
})
export class PaginationModule {
    public static forRoot(): ModuleWithProviders<PaginationModule> {
        return {
            ngModule: PaginationModule,
        };
    }
}
