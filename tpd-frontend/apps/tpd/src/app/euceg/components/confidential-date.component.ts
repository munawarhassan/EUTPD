import { Component, ElementRef, Input, OnDestroy, OnInit, Renderer2, Self, ViewChild } from '@angular/core';
import { NgControl, Validators } from '@angular/forms';
import { ConfidentialValue, Euceg } from '@devacfr/euceg';
import { FlatPickrDirective, ValueAccessorBase } from '@devacfr/forms';
import { isArray } from 'lodash-es';
import { Subscription } from 'rxjs';
import { ConfidentialComponent } from './confidential.component';

@Component({
    selector: 'app-confidential-date',
    templateUrl: './confidential-date.component.html',
})
export class ConfidentialDateComponent
    extends ValueAccessorBase<ConfidentialValue<string> | undefined>
    implements OnInit, OnDestroy
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
    public placeholder: string | undefined;

    @Input()
    public required = false;

    // eslint-disable-next-line @angular-eslint/no-input-rename
    @Input('enforce-confidential')
    public enforceConfidential: boolean | undefined;

    public altDateFormat = 'Y-m-d';

    public dateInputFormat = 'Y-m-d';

    @ViewChild(FlatPickrDirective, { static: true })
    private _flatpickr!: FlatPickrDirective;

    @ViewChild(ConfidentialComponent, { static: true })
    private _checkElement!: ConfidentialComponent;

    private _subscription = new Subscription();

    constructor(@Self() ngControl: NgControl, private _elementRef: ElementRef, _renderer: Renderer2) {
        super(ngControl, _renderer);
        this.ngControl.valueAccessor = this;
    }

    ngOnInit(): void {
        if (!this.required) {
            this.required = this.hasValidator(Validators.required);
        }
    }

    ngOnDestroy(): void {
        this._subscription.unsubscribe();
    }
    public handleInput(value: Date | (Date | undefined)[] | undefined): void {
        const date = isArray(value) ? value[0] : value;
        const val = this._flatpickr.formatDate(date);
        if (val) {
            if (!this.value) {
                let confidential = this._checkElement.value;
                if (this.enforceConfidential != null) {
                    confidential = this.enforceConfidential;
                }
                if (confidential == null) {
                    confidential = false;
                }
                this.value = {
                    value: val,
                    confidential,
                };
            } else {
                this.value.value = val;
            }
        } else {
            this.value = undefined;
        }
    }

    public handleChangeCheckbox(confidential?: boolean) {
        if (this.value) {
            this.value.confidential = confidential ?? false;
        }
    }

    public override writeValue(value: ConfidentialValue<string> | undefined) {
        super.writeValue(value);
        this._flatpickr?.writeValue(value?.value);
        this._checkElement?.writeValue(value?.confidential);
    }

    public override setDisabledState(isDisabled: boolean): void {
        super.setDisabledState(isDisabled);
        this._flatpickr?.setDisabledState(isDisabled);
    }
}
