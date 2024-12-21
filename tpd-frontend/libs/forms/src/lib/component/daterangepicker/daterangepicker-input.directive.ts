import { Directive, ElementRef, forwardRef, Host, HostListener, OnDestroy, OnInit } from '@angular/core';
import {
    AbstractControl,
    ControlValueAccessor,
    NG_VALIDATORS,
    NG_VALUE_ACCESSOR,
    ValidationErrors,
    Validator,
} from '@angular/forms';
import { isArray } from 'lodash-es';
import moment from 'moment';
import { Subscription } from 'rxjs';
import { DateRangePickerDirective } from './daterangepicker.directive';
import { DaterangepickerType } from './typing';

function isDateValid(date: Date | undefined): boolean {
    return date != null && date.getTime && !isNaN(date.getTime());
}

function convert(
    mode: 'single' | 'range',
    value: string | Date | (Date | undefined)[] | (string | undefined)[] | undefined
): Date | (Date | undefined)[] | undefined {
    if (value == null) return value;
    if (mode === 'single') {
        const val = isArray(value) ? value[0] : value;
        return val ? moment(val).toDate() : undefined;
    }
    let ar = isArray(value) ? value : [value];
    if (mode === 'range') {
        ar = ar.slice(0, 2);
    }
    return ar.map((v) => (v ? moment(v).toDate() : undefined)).slice(0, 2);
}

function extractRange(value?: Date | (Date | undefined)[] | undefined): DaterangepickerType {
    if (value == null) {
        return {};
    }
    const ar = isArray(value) ? value : [value];
    return {
        startDate: ar[0],
        endDate: ar[1],
    };
}

@Directive({
    selector: 'input[ltDateRangePickr]',
    exportAs: 'ltDateRangePickrInput',
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: DateRangePickerInputDirective,
            multi: true,
        },
        {
            provide: NG_VALIDATORS,
            useExisting: forwardRef(() => DateRangePickerInputDirective),
            multi: true,
        },
    ],
})
export class DateRangePickerInputDirective implements OnInit, OnDestroy, ControlValueAccessor, Validator {
    private _value: Date | (Date | undefined)[] | undefined;
    protected _disabled = false;

    private subscriptions = new Subscription();

    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    private _onChange = (_: unknown) => {
        //noop
    };

    // eslint-disable-next-line @typescript-eslint/no-empty-function
    private _onTouched = () => {};

    /**
     *
     * @param _element
     * @param _renderer
     * @param _cd
     */
    constructor(@Host() private _picker: DateRangePickerDirective, private _element: ElementRef) {}

    ngOnInit(): void {
        this.subscriptions.add(
            this._picker.changedRange.subscribe(
                (event) => (this.value = [event.range?.startDate, event.range?.endDate])
            )
        );
    }

    ngOnDestroy(): void {
        this.subscriptions.unsubscribe();
    }

    /**
     *
     */
    public get value(): Date | (Date | undefined)[] | undefined {
        return this._value;
    }

    /**
     *
     */
    public set value(value: Date | (Date | undefined)[] | undefined) {
        if (this._value !== value) {
            this._value = convert(this._picker.mode, value);
            this.markAsChanged();
            this.markAsTouched();
        }
    }

    /**
     *
     */
    @HostListener('blur')
    onBlur(): void {
        this._onTouched();
    }

    /**
     *
     */
    public get disabled(): boolean {
        return this._disabled;
    }

    /**
     *
     * @param isDisabled
     */
    public setDisabledState?(isDisabled: boolean): void {
        this._disabled = isDisabled;
    }

    /**
     *
     * @param value
     */
    public writeValue(value: string | Date | (Date | undefined)[] | (string | undefined)[] | undefined): void {
        this._value = convert(this._picker.mode, value);
        this._picker.range = extractRange(this._value);
    }

    /**
     *
     * @param fn
     */
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    registerOnChange(fn: any): void {
        this._onChange = fn;
    }
    /**
     *
     * @param fn
     */
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    registerOnTouched(fn: any): void {
        this._onTouched = fn;
    }
    /**
     *
     * @param control
     * @returns
     */
    validate(control: AbstractControl): ValidationErrors | null {
        let _value;
        if (control.value) {
            _value = convert(this._picker.mode, control.value);
        }
        const errors: Record<string, unknown>[] = [];

        if (_value == null || !isArray(_value)) {
            return null;
        }

        const _isFirstDateValid = isDateValid(_value[0]);
        const _isSecondDateValid = isDateValid(_value[1]);

        if (!_isFirstDateValid) {
            return { invalid: { startDate: _value[0] } };
        }

        if (!_isSecondDateValid && this._picker.mode === 'range') {
            return { invalid: { endDate: _value[1] } };
        }

        const minDate = this._picker.minDate ? moment(this._picker.minDate).toDate() : undefined;
        const maxDate = this._picker.maxDate ? moment(this._picker.maxDate).toDate() : undefined;

        if (minDate && _value[0] && _value[0] < minDate) {
            _value[0] = minDate;
            errors.push({ minDate: { minDate } });
        }

        if (maxDate && _value[1] && _value[1] > maxDate) {
            _value[1] = maxDate;
            errors.push({ maxDate: { maxDate } });
        }
        if (errors.length > 0) {
            return errors;
        }

        return null;
    }

    /**
     *
     */
    public markAsTouched() {
        this._onTouched();
    }

    /**
     *
     */
    public markAsChanged() {
        let val: Date | (Date | undefined)[] | (string | undefined)[] | undefined = this.value;
        if (this._picker.mode == 'single' && isArray(this.value) && this.value.length > 0) {
            val = moment(this.value[0]).toDate();
        }
        this._onChange(val);
    }
}
