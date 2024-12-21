import { CommonModule } from '@angular/common';
import { ModuleWithProviders, NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { InlineSVGModule } from '../inline-svg';

import { BreadcrumbComponent } from './breadcrumb.component';
import { BreadcrumbService } from './breadcrumb.service';

@NgModule({
    imports: [CommonModule, RouterModule, InlineSVGModule],
    exports: [BreadcrumbComponent],
    declarations: [BreadcrumbComponent],
    providers: [],
})
export class BreadcrumbModule {
    static forRoot(): ModuleWithProviders<BreadcrumbModule> {
        return {
            ngModule: BreadcrumbModule,
            providers: [BreadcrumbService],
        };
    }
}
