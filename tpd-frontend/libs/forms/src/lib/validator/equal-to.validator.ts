import { Directive, forwardRef, Input } from '@angular/core';
import { AbstractControl, NG_VALIDATORS, ValidationErrors, Validator, ValidatorFn } from '@angular/forms';

export const EQUAL_TO_VALIDATOR: any = {
    provide: NG_VALIDATORS,
    // eslint-disable-next-line @angular-eslint/no-forward-ref
    useExisting: forwardRef(() => EqualToValidator),
    multi: true,
};

function isEmptyInputValue(value: any): boolean {
    // we don't check for string here so it also works with arrays
    return value == null || value.length === 0;
}

@Directive({
    // eslint-disable-next-line @angular-eslint/directive-selector
    selector: '[equalTo][formControlName],[equalTo][formControl],[equalTo][ngModel]',
    providers: [EQUAL_TO_VALIDATOR],
})
export class EqualToValidator implements Validator {
    private _enabled: boolean | undefined;
    private _field: string | undefined;

    public static equalTo(field: string): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            if (isEmptyInputValue(control.value) || isEmptyInputValue(field)) {
                return null; // don't validate empty values to allow optional controls
            }
            return EqualToValidator._validate(field, control);
        };
    }

    private static _validate(field: string | undefined, c: AbstractControl): ValidationErrors | null {
        if (!field) {
            return null;
        }
        // self value
        const v = c.value;

        // control vlaue
        const e = c.root.get(field);

        const isEquals = e && v === e.value;

        if (!isEquals) {
            return {
                equalTo: {
                    actual: v,
                    expected: e,
                },
            };
        }
        return null;
    }

    @Input()
    set equalTo(value: string) {
        this._enabled = value != null || value !== undefined;
        this._field = value;
    }

    public validate(c: AbstractControl): ValidationErrors | null {
        return EqualToValidator._validate(this._field, c);
    }
}
