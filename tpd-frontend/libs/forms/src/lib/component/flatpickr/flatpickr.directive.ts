import {
    Directive,
    ElementRef,
    EventEmitter,
    HostBinding,
    HostListener,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    Optional,
    Output,
    Renderer2,
    Self,
    SimpleChanges,
} from '@angular/core';
import { AbstractControl, ControlValueAccessor, NgControl, ValidationErrors, Validator } from '@angular/forms';
import flatpickr from 'flatpickr';
import { isArray } from 'lodash-es';
import { Subscription } from 'rxjs';
import { synchronizeStatusChange } from '../shared';

function isDateValid(date: Date | undefined): boolean {
    return date != null && date.getTime && !isNaN(date.getTime());
}

@Directive({
    selector: 'input[ltFlatpickr]',
    exportAs: 'ltFlatpickr',
})
export class FlatPickrDirective implements OnInit, OnDestroy, OnChanges, ControlValueAccessor, Validator {
    @HostBinding('autocomplete')
    protected autocomplete = 'off';

    /**
     * Exactly the same as date format, but for the altInput field
     */
    @Input()
    public altFormat: string | undefined;

    @Input()
    public allowInput: boolean | undefined;

    /**
     * Enables time picker
     */
    @Input()
    public enableTime: boolean | undefined;

    /**
     * A string of characters which are used to define how the date will be displayed in the input box.
     */
    @Input()
    public dateFormat: string | undefined;

    @Input()
    public minDate: string | Date | undefined;

    @Input()
    public maxDate: string | Date | undefined;
    /**
     * "single", "multiple", or "range"
     */
    @Input()
    public mode: 'single' | 'multiple' | 'range' = 'single';

    @Output()
    public valueChange = new EventEmitter<Date | (Date | undefined)[] | undefined>();

    private _calendar: flatpickr.Instance | undefined;

    public touched = false;

    protected _disabled = false;

    private _value: Date | (Date | undefined)[] | undefined = undefined;
    private _subscription = new Subscription();
    private _unlistener: (() => void)[] = [];

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
    constructor(
        @Self() @Optional() private _ngControl: NgControl,
        private _element: ElementRef,
        private _renderer: Renderer2
    ) {
        if (this._ngControl) {
            this._ngControl.valueAccessor = this;
            this._ngControl.control?.setValidators(this.validate.bind(this));
            this._ngControl.control?.updateValueAndValidity();
            this._subscription.add(
                this._ngControl.control?.statusChanges?.subscribe(() => {
                    // update angular validation class in alt input element
                    setTimeout(() => {
                        if (this._calendar?.altInput) {
                            synchronizeStatusChange(this._element, this._calendar?.altInput);
                        }
                    });
                })
            );
        }
    }
    /**
     *
     */
    ngOnInit(): void {
        const options: flatpickr.Options.Options = {
            enableTime: this.enableTime,
            dateFormat: this.dateFormat,
            altFormat: this.altFormat,
            altInput: this.altFormat != null,
            mode: this.mode,
            allowInput: this.allowInput,
            minDate: this.minDate,
            maxDate: this.maxDate,
            onChange: this.onChange.bind(this),
        };

        this._calendar = flatpickr(this._element.nativeElement, options);
        if (this._calendar.altInput) {
            this._unlistener.push(
                this._renderer.listen(this._calendar.altInput, 'blur', this.markAsTouched.bind(this))
            );
            // this._unlistener.push(this._renderer.listen(this._calendar.altInput, 'change', this.markAsChanged.bind(this)));
        }
    }

    /**
     *
     */
    ngOnDestroy(): void {
        this._unlistener.forEach((unlisten) => unlisten());
        this._subscription.unsubscribe();
        this._calendar?.destroy();
    }

    /**
     *
     * @param changes
     */
    ngOnChanges(changes: SimpleChanges): void {
        if (changes.enableTime && changes.enableTime.currentValue !== changes.enableTime.previousValue) {
            this._calendar?.set('enableTime', changes.enableTime.currentValue);
        }
        if (changes.dateFormat && changes.dateFormat.currentValue !== changes.dateFormat.previousValue) {
            this._calendar?.set('dateFormat', changes.dateFormat.currentValue);
        }
        if (changes.mode && changes.mode.currentValue !== changes.mode.previousValue) {
            this._calendar?.set('mode', changes.mode.currentValue);
        }
        if (changes.allowInput && changes.allowInput.currentValue !== changes.allowInput.previousValue) {
            this._calendar?.set('allowInput', changes.allowInput.currentValue);
        }
        if (changes.minDate && changes.minDate.currentValue !== changes.minDate.previousValue) {
            this._calendar?.set('minDate', changes.minDate.currentValue);
        }
        if (changes.maxDate && changes.maxDate.currentValue !== changes.maxDate.previousValue) {
            this._calendar?.set('maxDate', changes.maxDate.currentValue);
        }
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
            this._value = value;
            this.markAsChanged();
            this.markAsTouched();
        }
    }

    /**
     *
     */
    @HostListener('blur')
    onBlur(): void {
        this.markAsTouched();
    }

    /**
     *
     */
    onChange(): void {
        this.value = this._calendar?.selectedDates;
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
    public setDisabledState(isDisabled: boolean): void {
        this._disabled = isDisabled;
        this._renderer.setProperty(this._element.nativeElement, 'disabled', isDisabled);
        if (this._calendar?.altInput) {
            this._renderer.setProperty(this._calendar?.altInput, 'disabled', isDisabled);
        }
    }

    /**
     *
     * @param value
     */
    public writeValue(value: string | Date | (string | undefined)[] | (Date | undefined)[] | undefined): void {
        if (!this._calendar) return;
        this._value = this.parseDate(value);
        // workaroung: use any, wrong type definition
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        this._calendar.setDate(this._value as any);
    }

    public parseDate(
        value: string | Date | (Date | undefined)[] | (string | undefined)[] | undefined
    ): Date | (Date | undefined)[] | undefined {
        if (value == null) return value;
        const mode = this._calendar?.config.mode;
        if (mode === 'single') {
            const val = isArray(value) ? value[0] : value;
            return val ? this._calendar?.parseDate(val) : undefined;
        }
        let ar = isArray(value) ? value : [value];
        if (mode === 'range') {
            ar = ar.slice(0, 2);
        }
        return ar.map((v) => (v ? this._calendar?.parseDate(v) : undefined)).slice(0, 2);
    }

    public formatDate(value: Date | undefined, fmt?: string): string | undefined {
        if (!value) {
            return undefined;
        }
        const format = fmt ?? this.dateFormat;
        if (!format) {
            return undefined;
        }
        return this._calendar?.formatDate(value, format);
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
        if (this._calendar == null) {
            return null;
        }
        if (control.value) {
            const ar = isArray(control.value) ? control.value : [control.value];
            _value = ar.map((v) => (v ? this._calendar?.parseDate(v) : undefined));
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

        if (!_isSecondDateValid && this.mode === 'range') {
            return { invalid: { endDate: _value[1] } };
        }

        const minDate = this.minDate ? this._calendar?.parseDate(this.minDate) : undefined;
        const maxDate = this.maxDate ? this._calendar?.parseDate(this.maxDate) : undefined;

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
        if (!this.touched) {
            this._onTouched();
            this.touched = true;
            if (this._calendar?.altInput) {
                this._renderer.removeClass(this._calendar.altInput, 'ng-untouched');
                this._renderer.addClass(this._calendar.altInput, 'ng-touched');
            }
        }
    }

    /**
     *
     */
    public markAsChanged() {
        let val: Date | (Date | undefined)[] | undefined = this.value;
        if (this.mode == 'single' && isArray(this.value) && this.value.length > 0) {
            val = this.value[0];
        }
        this._onChange(val);
        this.valueChange.emit(val);
    }
}
