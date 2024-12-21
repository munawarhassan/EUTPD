import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { DirectivesModule, InlineSVGModule } from '@devacfr/bootstrap';

import { BreadcrumbPathComponent } from './breadcrumb-path.component';

@NgModule({
    imports: [CommonModule, InlineSVGModule, DirectivesModule],
    exports: [BreadcrumbPathComponent],
    declarations: [BreadcrumbPathComponent],
    providers: [],
})
export class BreadcrumbPathModule {}
