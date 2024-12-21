import {
    AfterViewInit,
    Component,
    ElementRef,
    HostBinding,
    Input,
    OnDestroy,
    OnInit,
    Renderer2,
    Self,
} from '@angular/core';
import { NgControl, ValidatorFn, Validators } from '@angular/forms';
import { ClassBuilder } from '@devacfr/util';
import { Subscription } from 'rxjs';
import { synchronizeStatusChange } from '../shared';
import { ValueAccessorBase } from './value-accessor-base';
import { uniqueId } from 'lodash-es';

@Component({
    // eslint-disable-next-line @angular-eslint/component-selector
    selector: 'form-text',
    templateUrl: './form-text.component.html',
})
export class FormTextComponent
    extends ValueAccessorBase<string | undefined>
    implements OnInit, AfterViewInit, OnDestroy
{
    @HostBinding('class')
    public class = 'd-block';

    @Input()
    public type = 'text';

    @Input()
    public name: string | undefined;

    @Input()
    public label: string | undefined;

    @Input()
    public icon: string | undefined;

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
        const builder = ClassBuilder.create('form-control');
        if (this._controlClass) {
            builder.css(this._controlClass);
        }
        if (this.iconPosition === 'start' && this.icon) {
            builder.css('ps-16');
        }
        return builder.toString();
    }

    public set controlClass(value: string) {
        this._controlClass = value;
    }

    @Input()
    public iconCLass = 'svg-icon-1';

    @Input()
    public iconPosition: 'start' | 'end' = 'start';

    public inputId: string;

    @Input()
    public required = false;

    private _controlClass: string | undefined;
    private _inputElement: HTMLElement | null = null;
    private _subscription = new Subscription();

    constructor(@Self() ngControl: NgControl, private _elementRef: ElementRef, _renderer: Renderer2) {
        super(ngControl, _renderer);
        this.ngControl.valueAccessor = this;
        this.inputId = uniqueId('flexTextbox');
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
        this.setPropertyValue(this.value);
    }

    ngOnDestroy(): void {
        this._subscription.unsubscribe();
    }

    public handleInput(event: Event): void {
        const value: string | null = (event.target as HTMLInputElement).value;
        this.value = value;
    }

    public get fieldName(): string | undefined {
        if (this.label) return this.label;
        return this.placeholder;
    }

    override writeValue(value: string | undefined) {
        super.writeValue(value);
        this.setPropertyValue(value);
    }

    private setPropertyValue(value: string | undefined) {
        if (this._inputElement) {
            const val = value ? value : '';
            this._renderer.setProperty(this._inputElement, 'value', val);
        }
    }
}
