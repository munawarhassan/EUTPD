import {
    AfterViewInit,
    Component,
    ElementRef,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    Renderer2,
    Self,
    SimpleChanges,
    ViewChild,
} from '@angular/core';
import { NgControl, Validators } from '@angular/forms';
import { ClassBuilder } from '@devacfr/util';
import { isArray } from 'lodash-es';
import { Subscription } from 'rxjs';
import { FormSelectObserver, FormSelectOptions } from './typing';
import { Select2Data, Select2Directive, Select2Observer } from '../select2';
import { ValueAccessorBase } from './value-accessor-base';

@Component({
    // eslint-disable-next-line @angular-eslint/component-selector
    selector: 'form-select',
    templateUrl: './form-select.component.html',
})
export class FormSelectComponent
    extends ValueAccessorBase<Select2Data>
    implements OnInit, AfterViewInit, OnDestroy, OnChanges
{
    @Input()
    public name: string | undefined;

    @Input()
    public label: string | undefined;

    @Input()
    public helpText: string | undefined;

    @Input()
    public info: string | undefined;

    @Input()
    public placeholder: string | undefined;

    @Input()
    public options: FormSelectOptions | FormSelectObserver | unknown | undefined;

    @Input()
    public allowClear = false;

    @Input()
    public searchable = false;

    @Input()
    public optionValue = 'value';

    @Input()
    public optionText = 'name';

    @ViewChild(Select2Directive, { static: true, read: Select2Directive })
    public select2: Select2Directive | undefined;

    @Input()
    public get controlClass(): string {
        const builder = ClassBuilder.create('form-select');
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

    public options$: Select2Data | Select2Observer<Select2Data> | undefined;

    private _controlClass: string | undefined;
    private _subscription = new Subscription();

    constructor(@Self() ngControl: NgControl, private _elementRef: ElementRef, _renderer: Renderer2) {
        super(ngControl, _renderer);
        this.ngControl.valueAccessor = this;
    }
    ngOnInit(): void {
        this._subscription.add(
            this.ngControl.control?.statusChanges?.subscribe(() => {
                // update angular validation class in alt input element
                this.select2?.synchronizeStatusChange(this._elementRef);
            })
        );
        if (!this.required) {
            this.required = this.hasValidator(Validators.required);
        }
    }

    ngAfterViewInit(): void {
        this.select2?.synchronizeStatusChange(this._elementRef);
    }

    ngOnDestroy(): void {
        this._subscription.unsubscribe();
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.options) {
            this.options$ = changes.options.currentValue;
        }
    }

    public handleValueChange(value: string | (string | undefined)[] | undefined) {
        if (!isArray(value)) {
            this.value = value;
        }
    }

    public override writeValue(value: Select2Data): void {
        super.writeValue(value);
        this.select2?.writeValue(value);
    }
}
