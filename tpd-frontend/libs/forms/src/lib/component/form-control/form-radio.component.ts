import { Component, ElementRef, Injectable, Input, NgModule, OnDestroy, OnInit, Renderer2, Self } from '@angular/core';
import { NgControl, Validators } from '@angular/forms';
import { ClassBuilder } from '@devacfr/util';
import { uniqueId } from 'lodash-es';
import { ValueAccessorBase } from './value-accessor-base';

/**
 * Internal-only NgModule that works as a host for the `RadioControlRegistry` tree-shakable
 * provider. Note: the `InternalFormsSharedModule` can not be used here directly, since it's
 * declared *after* the `RadioControlRegistry` class and the `providedIn` doesn't support
 * `forwardRef` logic.
 */
@NgModule()
export class RadioControlRegistryModule {}

/**
 * @description
 * Class used by Angular to track radio buttons. For internal use only.
 */
@Injectable({ providedIn: RadioControlRegistryModule })
export class RadioControlRegistry {
    private _accessors: { control: NgControl; accessor: FormRadioComponent }[] = [];

    /**
     * @description
     * Adds a control to the internal registry. For internal use only.
     */
    add(control: NgControl, accessor: FormRadioComponent) {
        this._accessors.push({ control, accessor });
    }

    /**
     * @description
     * Removes a control from the internal registry. For internal use only.
     */
    remove(accessor: FormRadioComponent) {
        for (let i = this._accessors.length - 1; i >= 0; --i) {
            if (this._accessors[i].accessor === accessor) {
                this._accessors.splice(i, 1);
                return;
            }
        }
    }

    /**
     * @description
     * Selects a radio button. For internal use only.
     */
    select(accessor: FormRadioComponent) {
        this._accessors.forEach((c) => {
            if (this._isSameGroup(c, accessor) && c.accessor !== accessor) {
                c.accessor.fireUncheck(accessor.value);
            }
        });
    }

    private _isSameGroup(
        controlPair: { control: NgControl; accessor: FormRadioComponent },
        accessor: FormRadioComponent
    ): boolean {
        if (!controlPair.control.control) return false;
        return (
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            (controlPair.control as any)._parent === (accessor.ngControl as any)._parent &&
            controlPair.accessor.name === accessor.name
        );
    }
}

@Component({
    // eslint-disable-next-line @angular-eslint/component-selector
    selector: 'form-radio',
    templateUrl: './form-radio.component.html',
})
export class FormRadioComponent extends ValueAccessorBase<number | string> implements OnInit, OnDestroy {
    @Input()
    public name: string | undefined;

    @Input()
    public formControlName!: string;

    @Input()
    public label: string | undefined;

    @Input()
    public info: string | undefined;

    @Input()
    public switch = false;

    override get value(): number | string | undefined {
        return this._value;
    }

    @Input()
    override set value(value: number | string | undefined) {
        super._value = value;
    }

    @Input()
    public get controlClass(): string {
        const builder = ClassBuilder.create('form-check form-check-custom');
        if (this._controlClass) {
            builder.css(this._controlClass);
        }
        if (this.switch) {
            builder.css('form-switch');
        }
        return builder.toString();
    }

    public set controlClass(value: string) {
        this._controlClass = value;
    }

    @Input()
    public required: boolean | undefined;

    private _controlClass: string | undefined;

    public inputId: string;

    public _state = false;

    private _inputElement: Element | null = null;

    constructor(
        @Self() ngControl: NgControl,
        private _elementRef: ElementRef,
        private _registry: RadioControlRegistry,
        _renderer: Renderer2
    ) {
        super(ngControl, _renderer);
        this.inputId = uniqueId('flexRadio');
        this.ngControl.valueAccessor = this;
    }

    ngOnInit(): void {
        const el = this._elementRef.nativeElement as HTMLElement;
        this._inputElement = el.querySelector('input');
        if (!this.required) {
            this.required = this.hasValidator(Validators.required);
        }
        this._registry.add(this.ngControl, this);
        this._checkName();
        this.setPropertyValue(this._state);
    }

    ngOnDestroy(): void {
        this._registry.remove(this);
    }

    public handleChange(): void {
        this.markAsChanged();
    }

    override writeValue(value: number | string | undefined) {
        this._state = value === this.value;
        this.setPropertyValue(this._state);
    }

    // eslint-disable-next-line @typescript-eslint/ban-types
    override registerOnChange(fn: (_: unknown) => {}): void {
        super.registerOnChange(() => {
            fn(this.value);
            this._registry.select(this);
        });
    }

    /**
     * Sets the "value" on the radio input element and unchecks it.
     *
     * @param value
     */
    fireUncheck(value: number | string | undefined): void {
        this.writeValue(value);
    }

    private _checkName(): void {
        // if (
        //     this.name &&
        //     this.formControlName &&
        //     this.name !== this.formControlName &&
        //     (typeof ngDevMode === 'undefined' || ngDevMode)
        // ) {
        //     throw new Error(`
        //     If you define both a name and a formControlName attribute on your radio button, their values
        //     must match. Ex: <input type="radio" formControlName="food" name="food">
        //   `);
        // }
        if (!this.name && this.formControlName) this.name = this.formControlName;
    }

    private setPropertyValue(value: boolean | undefined) {
        if (this._inputElement) {
            this._renderer.setProperty(this._inputElement, 'checked', value);
        }
    }
}
