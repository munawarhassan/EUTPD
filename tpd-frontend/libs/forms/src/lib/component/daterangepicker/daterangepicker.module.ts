import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonRangepickerComponent } from './button-rangepicker.component';
import { DateRangePickerInputDirective } from './daterangepicker-input.directive';
import { DateRangePickerDirective } from './daterangepicker.directive';

@NgModule({
    imports: [CommonModule, FormsModule],
    exports: [DateRangePickerInputDirective, DateRangePickerDirective, ButtonRangepickerComponent],
    declarations: [DateRangePickerInputDirective, DateRangePickerDirective, ButtonRangepickerComponent],
    providers: [],
})
export class DateRangePickerModule {}
