import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgApexchartsModule } from 'ng-apexcharts';

import { ChartRecentComponent } from './chart-recent.component';

@NgModule({
    imports: [CommonModule, FormsModule, ReactiveFormsModule, NgApexchartsModule],
    exports: [ChartRecentComponent],
    declarations: [ChartRecentComponent],
    providers: [],
})
export class ChartRecentModule {}
