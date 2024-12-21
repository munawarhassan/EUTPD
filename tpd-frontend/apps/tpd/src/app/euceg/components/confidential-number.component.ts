import {
    AfterViewInit,
    Component,
    ElementRef,
    Input,
    OnDestroy,
    OnInit,
    Renderer2,
    Self,
    ViewChild,
} from '@angular/core';
import { NgControl, Validators } from '@angular/forms';
import { ConfidentialValue } from '@devacfr/euceg';
import { synchronizeStatusChange, ValueAccessorBase } from '@devacfr/forms';
import { ClassBuilder } from '@devacfr/util';
import { Subscription } from 'rxjs';
import { ConfidentialComponent } from './confidential.component';

@Component({
    selector: 'app-confidential-number',
    templateUrl: './confidential-number.component.html',
})
export class ConfidentialNumberComponent
    extends ValueAccessorBase<ConfidentialValue<string> | undefined>
    implements OnInit, AfterViewInit, OnDestroy
{
    @Input()
    public type = 'text';

    // eslint-disable-next-line @angular-eslint/no-input-rename
    @Input('enforce-confidential')
    public enforceConfidential: boolean | undefined;

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
    public autocomplete = true;

    @Input()
    public get controlClass(): string {
        const builder = ClassBuilder.create('form-control form-control-solid');
        if (this._controlClass) {
            builder.css(this._controlClass);
        }
        return builder.toString();
    }

    public set controlClass(value: string) {
        this._controlClass = value;
    }

    @Input()
    public required = false;

    private _controlClass: string | undefined;
    private _inputElement: HTMLElement | null = null;

    @ViewChild(ConfidentialComponent, { static: true })
    private _checkElement!: ConfidentialComponent;

    private _subscription = new Subscription();

    constructor(@Self() ngControl: NgControl, private _elementRef: ElementRef, _renderer: Renderer2) {
        super(ngControl, _renderer);
        this.ngControl.valueAccessor = this;
    }

    ngOnInit(): void {
        this._subscription.add(
            this.ngControl.control?.statusChanges?.subscribe(() => {
                // update angular validation class in alt input element
                setTimeout(() => {
                    if (this._inputElement) {
                        synchronizeStatusChange(this._elementRef, this._inputElement);
                    }
                });
            })
        );
        const el = this._elementRef.nativeElement as HTMLElement;
        this._inputElement = el.querySelector('input');
        if (!this.required) {
            this.required = this.hasValidator(Validators.required);
        }
    }

    ngAfterViewInit(): void {
        if (this._inputElement) {
            synchronizeStatusChange(this._elementRef, this._inputElement);
        }
        this.setInputValue(this.value?.value);
        this._checkElement?.writeValue(this.value?.confidential);
    }

    ngOnDestroy(): void {
        this._subscription.unsubscribe();
    }

    public handleInput(event: Event): void {
        const value: string | null = (event.target as HTMLInputElement).value;
        if (value && value !== '') {
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

    public handleChangeCheckbox(confidential?: boolean) {
        if (this.value) {
            this.value.confidential = confidential ?? false;
        }
    }

    public override writeValue(value: ConfidentialValue<string> | undefined) {
        super.writeValue(value);
        this.setInputValue(value?.value);
        this._checkElement?.writeValue(value?.confidential);
    }

    private setInputValue(value: string | undefined) {
        if (this._inputElement) {
            const val = value ? value : '';
            this._renderer.setProperty(this._inputElement, 'value', val);
        }
    }
}
