import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DirectivesModule, InlineSVGModule } from '@devacfr/bootstrap';
import { DateRangePickerModule, FormControlModule } from '@devacfr/forms';
import { MenuModule } from '@devacfr/layout';
import { SubmissionFilterResultComponent } from './submission-filter-result.component';
import { SubmissionFilterComponent } from './submission-filter.component';

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
        DateRangePickerModule,
    ],
    exports: [SubmissionFilterComponent, SubmissionFilterResultComponent],
    declarations: [SubmissionFilterComponent, SubmissionFilterResultComponent],
    providers: [],
})
export class SubmissionFilterModule {}
