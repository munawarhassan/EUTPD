import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import moment from 'moment';
import { BehaviorSubject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ChangedValueEvent, DaterangepickerType } from './typing';

@Component({
    selector: 'lt-button-rangepicker',
    templateUrl: './button-rangepicker.component.html',
})
export class ButtonRangepickerComponent implements OnChanges {
    public rangeText = '';

    @Input()
    public range: DaterangepickerType | undefined;

    @Output()
    public rangeChange = new EventEmitter<DaterangepickerType>();

    public range$: Observable<DaterangepickerType>;
    public rangeEmitter: BehaviorSubject<ChangedValueEvent>;

    constructor() {
        this.rangeEmitter = new BehaviorSubject({
            range: this.range,
            option: { emitEvent: false },
        } as ChangedValueEvent);
        this.range$ = this.rangeEmitter.pipe(
            map((event) => {
                this.onChangedRange(event);
                this.range = event.range;
                if (event.option && event.option.emitEvent) {
                    this.rangeChange.emit(event.range);
                }
                return this.range;
            })
        );
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.range && changes.range.firstChange && changes.range.currentValue !== changes.range.previousValue) {
            this.rangeEmitter.next({
                range: changes.range.currentValue,
                option: { emitEvent: false },
            });
        }
    }

    private onChangedRange(event: ChangedValueEvent): void {
        let rangeText = event?.range?.label === 'Custom Range' ? undefined : event?.range?.label;
        if (!rangeText && event.range) {
            const range = event.range;
            const endDate = range.endDate ? moment(range.endDate) : undefined;
            const startDate = range.startDate ? moment(range.startDate) : undefined;

            if (endDate && startDate) {
                rangeText = startDate.format('MMM D') + ' - ' + endDate.format('MMM D');
            }
        }
        if (!rangeText) {
            rangeText = 'All';
        }

        this.rangeText = rangeText;
        if (event?.range) {
            event.range.label = rangeText;
        }
    }
}
