import { Injectable, Renderer2 } from '@angular/core';
import { ControlValueAccessor, FormGroupDirective, NgControl, NgForm, ValidatorFn } from '@angular/forms';
import _ from 'lodash-es';

@Injectable()
export abstract class ValueAccessorBase<T> implements ControlValueAccessor {
    public touched = false;

    protected _value: T | undefined;

    protected _disabled = false;

    protected _onChange = _.noop;

    protected _onTouched = _.noop;

    constructor(public ngControl: NgControl, protected _renderer: Renderer2) {}

    public get form(): NgForm | FormGroupDirective | undefined {
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        let parent = this.ngControl ? (this.ngControl as any)._parent : undefined;
        if (!parent) return undefined;
        while (parent._parent != null) {
            parent = parent._parent;
        }
        return parent;
    }

    public get value(): T | undefined {
        return this._value;
    }

    public set value(value: T | undefined) {
        if (this.disabled) return;
        if (this._value !== value) {
            this._value = value;
            this.markAsChanged();
        }
    }

    public get disabled(): boolean {
        return this._disabled;
    }

    public writeValue(value: T) {
        this._value = value;
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    public registerOnChange(fn: any): void {
        this._onChange = fn;
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    public registerOnTouched(fn: any): void {
        this._onTouched = fn;
    }

    public setDisabledState(isDisabled: boolean): void {
        this._disabled = isDisabled;
    }

    public markAsTouched() {
        this._onTouched();
    }

    public markAsChanged() {
        this._onChange(this.value);
    }

    public useSVG(icon: string): boolean {
        return icon != null && icon.endsWith('.svg');
    }

    protected hasValidator(validator: ValidatorFn): boolean {
        return !!this.ngControl?.control?.hasValidator(validator);
    }
}
