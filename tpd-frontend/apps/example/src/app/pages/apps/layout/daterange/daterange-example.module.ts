import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DateRangePickerModule, FlatPickrModule } from '@devacfr/forms';

import { DateRangeExampleComponent } from './daterange-example.component';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule.forChild([
            {
                path: '',
                component: DateRangeExampleComponent,
            },
        ]),
        FlatPickrModule,
        DateRangePickerModule,
    ],
    exports: [RouterModule],
    declarations: [DateRangeExampleComponent],
    providers: [],
})
export class DateRangeExampleModule {}
