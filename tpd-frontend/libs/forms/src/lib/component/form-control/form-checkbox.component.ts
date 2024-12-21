import { Component, ElementRef, HostBinding, Input, OnInit, Renderer2, Self } from '@angular/core';
import { NgControl, Validators } from '@angular/forms';
import { ClassBuilder } from '@devacfr/util';
import { uniqueId } from 'lodash-es';
import { ValueAccessorBase } from './value-accessor-base';

@Component({
    // eslint-disable-next-line @angular-eslint/component-selector
    selector: 'form-checkbox',
    templateUrl: './form-checkbox-component.html',
})
export class FormCheckboxComponent extends ValueAccessorBase<boolean | undefined> implements OnInit {
    @HostBinding('class')
    public class = 'd-block';

    @Input()
    public name: string | undefined;

    @Input()
    public label: string | undefined;

    @Input()
    public info: string | undefined;

    @Input()
    public helpText: string | undefined;

    @Input()
    public switch = false;

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

    public inputId: string;

    private _controlClass: string | undefined;

    private _inputElement: Element | null = null;

    constructor(@Self() ngControl: NgControl, private _elementRef: ElementRef, _renderer: Renderer2) {
        super(ngControl, _renderer);
        this.inputId = uniqueId('flexCheckbox');
        this.ngControl.valueAccessor = this;
    }

    ngOnInit(): void {
        if (!this.required) {
            this.required = this.hasValidator(Validators.required);
        }
        const el = this._elementRef.nativeElement as HTMLElement;
        this._inputElement = el.querySelector('input');
        this.setPropertyValue(this.value);
    }

    public handleChange(event: Event): void {
        const value: boolean | null = (event.target as HTMLInputElement).checked;
        this.value = value;
    }

    public override writeValue(value: boolean | undefined) {
        super.writeValue(value);
        this.setPropertyValue(value);
    }

    private setPropertyValue(value: boolean | undefined) {
        if (this._inputElement) {
            this._renderer.setProperty(this._inputElement, 'checked', value);
        }
    }
}
