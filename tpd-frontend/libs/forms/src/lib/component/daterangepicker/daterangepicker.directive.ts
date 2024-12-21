import {
    Directive,
    ElementRef,
    EventEmitter,
    HostBinding,
    Input,
    NgZone,
    OnDestroy,
    OnInit,
    Output,
} from '@angular/core';
import daterangepicker, { DataRangePickerCallback, DateOrString } from 'daterangepicker';
import moment from 'moment';
import { ChangedValueEvent, DaterangepickerType } from './typing';
import $ from 'jquery';

declare global {
    class daterangepicker {
        clickRange(e: Event): void;
        updateCalendars(): void;
    }
}

const override = daterangepicker.prototype.clickRange;
Object.defineProperty(daterangepicker.prototype, 'clickRange', {
    value: function (this: daterangepicker, ev) {
        let updated = false;
        if (!this.startDate.isValid()) {
            this.setStartDate(moment());
            updated = true;
        }
        if (!this.endDate.isValid()) {
            this.setEndDate(moment());
            updated = true;
        }
        if (updated) {
            this.updateCalendars();
        }
        override.call(this, ev);
    },
});

@Directive({
    selector: '[ltDateRangePickr]',
    exportAs: 'ltDateRangePickr',
})
export class DateRangePickerDirective implements OnInit, OnDestroy {
    @HostBinding('autocomplete')
    protected autocomplete = 'off';

    /**
     * A selector specifying the element the daterangepicker should be appended to.
     */
    @Input()
    public container = 'body';

    @Input()
    public mode: 'single' | 'range' = 'single';

    @Input()
    public minDate: string | Date | undefined;

    @Input()
    public maxDate: string | Date | undefined;

    @Input()
    public minYear: number | undefined;

    @Input()
    public maxYear: number | undefined;

    @Input()
    public align: 'left' | 'right' | 'center' = 'left';

    @Input()
    public showDropdowns = false;

    @Input()
    public showCustomRangeLabel: boolean | undefined;

    @Input()
    public ranges: Record<string, [DateOrString, DateOrString]> | undefined;

    @Input()
    public defaultRanges = false;

    @Input()
    public locale: daterangepicker.Locale | undefined = {
        format: 'MM/DD/YYYY',
    };

    /**
     * (true/false) Hide the apply and cancel buttons, and automatically apply a new
     * date range as soon as two dates are clicked.
     */
    @Input()
    public autoApply = true;

    @Output()
    public changeRange = new EventEmitter<DaterangepickerType>();

    @Output()
    public changedRange = new EventEmitter<ChangedValueEvent>();

    @Output()
    public cancel = new EventEmitter<DaterangepickerType | null>();

    @Output()
    public apply = new EventEmitter<DaterangepickerType | null>();

    @Output()
    public showCalendar = new EventEmitter<void>();

    @Output()
    public hideCalendar = new EventEmitter<void>();

    @Output()
    public showPicker = new EventEmitter<void>();

    @Output()
    public hidePicker = new EventEmitter<void>();

    public _calendar: daterangepicker | undefined;

    private _value: DaterangepickerType | undefined | null;

    /**
     *
     * @param _element
     * @param _renderer
     * @param _cd
     */
    constructor(private _element: ElementRef, private _zone: NgZone) {}
    /**
     *
     */
    ngOnInit(): void {
        this.daterangepickerInit();
    }

    /**
     *
     */
    ngOnDestroy(): void {
        this._calendar?.remove();
    }

    /**
     *
     */
    @Input()
    public get range(): DaterangepickerType | undefined | null {
        return this._value;
    }

    /**
     *
     */
    public set range(value: DaterangepickerType | undefined | null) {
        this._value = value;
        this._calendar?.setStartDate(this._value?.startDate ? this._value.startDate : '');
        this._calendar?.setEndDate(this._value?.endDate ? this._value.endDate : '');
        this.decorateText();
    }

    protected decorateText(): void {
        if (!this._calendar) return;
        const el = $(this._element.nativeElement);
        if (this.range == null) {
            el.val('');
            return;
        }
        let _format = 'MM/DD/YYYY';
        if (this.locale && this.locale.format) {
            _format = this.locale.format;
        }
        let text = '';
        if (this.range.startDate) {
            text += moment(this.range.startDate).format(_format);
        }
        if (this.range.endDate && this.mode == 'range') {
            text += ' to ' + moment(this.range.endDate).format(_format);
        }
        el.val(text);
    }

    protected daterangepickerInit() {
        const cb: DataRangePickerCallback = (
            startDate: moment.Moment,
            endDate: moment.Moment,
            label: string | null
        ) => {
            this.range = {
                startDate: startDate.isValid() ? startDate.toDate() : undefined,
                endDate: endDate.isValid() ? endDate.toDate() : undefined,
                label: label ? label : undefined,
            };
            this.changedRange.emit({ range: this.range, option: { emitEvent: true } });
            this.changeRange.emit(this.range);
        };

        const ranges: { [name: string]: [DateOrString, DateOrString] } | undefined = this.defaultRanges
            ? {
                  All: ['', ''],
                  Today: [moment(), moment()],
                  Yesterday: [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
                  'Last 7 Days': [moment().subtract(6, 'days'), moment()],
                  'Last 30 Days': [moment().subtract(29, 'days'), moment()],
                  'This Month': [moment().startOf('month'), moment().endOf('month')],
                  'Last Month': [
                      moment().subtract(1, 'month').startOf('month'),
                      moment().subtract(1, 'month').endOf('month'),
                  ],
              }
            : this.ranges;

        this._calendar = new daterangepicker(
            this._element.nativeElement,
            {
                parentEl: this.container,
                autoApply: this.autoApply,
                autoUpdateInput: false,
                startDate: this.range?.startDate,
                endDate: this.range?.endDate,
                minDate: this.minDate,
                maxDate: this.maxDate,
                minYear: this.minYear,
                maxYear: this.maxYear,
                singleDatePicker: this.mode === 'single',
                opens: this.align,
                showDropdowns: this.showDropdowns,
                ranges: ranges,
                showCustomRangeLabel: this.showCustomRangeLabel,
                locale: this.locale,
            },
            cb
        );

        const el = $(this._element.nativeElement);
        el.on('cancel.daterangepicker', () => {
            this.cancel.emit(this.range);
        });

        el.on('apply.daterangepicker', () => {
            this.decorateText();
            this.apply.emit(this.range);
        });

        el.on('show.daterangepicker', () => {
            this.showPicker.emit();
        });
        el.on('hide.daterangepicker', () => {
            this.decorateText();
            this.hidePicker.emit();
        });
        el.on('showCalendar.daterangepicker', () => {
            this.showCalendar.emit();
        });
        el.on('hideCalendar.daterangepicker', () => {
            this.hideCalendar.emit();
        });

        // enforce range
        this.range = this._value;
    }
}
