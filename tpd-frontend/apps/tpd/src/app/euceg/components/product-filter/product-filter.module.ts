import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DirectivesModule, InlineSVGModule } from '@devacfr/bootstrap';
import { FormControlModule } from '@devacfr/forms';
import { MenuModule } from '@devacfr/layout';
import { ProductFilterResultComponent } from './product-filter-result.component';
import { ProductFilterComponent } from './product-filter.component';

@NgModule({
    imports: [
        // angular modules
        CommonModule,
        FormsModule,
        // internal
        DirectivesModule,
        FormControlModule,
        MenuModule,
        InlineSVGModule,
    ],
    exports: [ProductFilterComponent, ProductFilterResultComponent],
    declarations: [ProductFilterComponent, ProductFilterResultComponent],
    providers: [],
})
export class ProductFilterModule {}
