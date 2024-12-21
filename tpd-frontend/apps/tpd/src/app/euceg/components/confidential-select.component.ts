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
import { ConfidentialValue } from '@devacfr/euceg';
import {
    FormSelectObserver,
    FormSelectOptions,
    Select2Data,
    Select2Directive,
    Select2Observer,
    ValueAccessorBase,
} from '@devacfr/forms';
import { isArray } from 'lodash-es';
import { Observable, Subscription } from 'rxjs';
import { ConfidentialComponent } from './confidential.component';

@Component({
    selector: 'app-confidential-select',
    templateUrl: './confidential-select.component.html',
})
export class ConfidentialSelectComponent
    extends ValueAccessorBase<ConfidentialValue | undefined>
    implements OnInit, AfterViewInit, OnDestroy, OnChanges
{
    @Input()
    public name: string | undefined;

    @Input()
    public options: FormSelectOptions | FormSelectObserver | Observable<FormSelectOptions> | unknown | undefined;

    @Input()
    public label: string | undefined;

    @Input()
    public helpText: string | undefined;

    @Input()
    public info: string | undefined;

    @Input()
    public placeholder: string | undefined;

    @Input()
    public required = false;

    @Input()
    public allowClear = false;

    @Input()
    public searchable = false;

    @Input()
    public optionValue = 'value';

    @Input()
    public optionText = 'name';

    // eslint-disable-next-line @angular-eslint/no-input-rename
    @Input('enforce-confidential')
    public enforceConfidential: boolean | undefined;

    public options$: Select2Data | Select2Observer<Select2Data> | undefined;

    @ViewChild(Select2Directive, { static: true, read: Select2Directive })
    private select2: Select2Directive | undefined;

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

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.options) {
            this.options$ = changes.options.currentValue;
        }
    }

    ngOnDestroy(): void {
        this._subscription.unsubscribe();
    }

    public handleSelectValueChange(value: string | (string | undefined)[] | undefined) {
        if (!isArray(value)) {
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

    public override writeValue(value: ConfidentialValue<string> | undefined): void {
        super.writeValue(value);
        this.select2?.writeValue(value?.value);
        this._checkElement?.writeValue(value?.confidential);
    }
}
