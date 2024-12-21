import { Component, ElementRef, Input, OnInit, Renderer2, Self, ViewChild } from '@angular/core';
import { NgControl, Validators } from '@angular/forms';
import { ConfidentialValue } from '@devacfr/euceg';
import { ValueAccessorBase } from '@devacfr/forms';
import { uniqueId } from 'lodash-es';
import { ConfidentialComponent } from './confidential.component';

@Component({
    selector: 'app-confidential-checkbox',
    templateUrl: './confidential-checkbox.component.html',
})
export class ConfidentialCheckboxComponent
    extends ValueAccessorBase<ConfidentialValue<boolean> | undefined>
    implements OnInit
{
    @Input()
    public name: string | undefined;

    @Input()
    public label: string | undefined;

    @Input()
    public info: string | undefined;

    @Input()
    public helpText: string | undefined;

    @Input()
    public required = false;

    public inputId: string;

    // eslint-disable-next-line @angular-eslint/no-input-rename
    @Input('enforce-confidential')
    public enforceConfidential: boolean | undefined;

    private _inputElement: Element | null = null;
    @ViewChild(ConfidentialComponent, { static: true })
    private _checkElement!: ConfidentialComponent;

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
        this.setPropertyValue(this.value?.value);
    }

    public handleChange(event: Event): void {
        const value = (event.target as HTMLInputElement).checked;
        if (value && value != null) {
            if (!this.value) {
                let confidential = this._checkElement.value;
                if (this.enforceConfidential != null) {
                    confidential = this.enforceConfidential;
                }
                if (confidential == null) {
                    confidential = false;
                }
                this.value = {
                    value,
                    confidential,
                };
            } else {
                this.value.value = value;
            }
        } else {
            this.value = undefined;
        }
    }

    public handleChangeConfidential(confidential?: boolean) {
        if (this.value) {
            this.value.confidential = confidential ?? false;
        }
    }

    public override writeValue(value: ConfidentialValue<boolean> | undefined) {
        super.writeValue(value);
        this.setPropertyValue(value?.value);
        this._checkElement?.writeValue(value?.confidential);
    }

    private setPropertyValue(value: boolean | undefined) {
        if (this._inputElement) {
            this._renderer.setProperty(this._inputElement, 'checked', value);
        }
    }
}
