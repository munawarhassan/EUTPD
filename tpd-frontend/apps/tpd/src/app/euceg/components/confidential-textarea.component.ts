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
import { Subscription } from 'rxjs';
import { ConfidentialComponent } from './confidential.component';

@Component({
    selector: 'app-confidential-textarea',
    templateUrl: './confidential-textarea.component.html',
})
export class ConfidentialTextareaComponent
    extends ValueAccessorBase<ConfidentialValue<string> | undefined>
    implements OnInit, AfterViewInit, OnDestroy
{
    @Input()
    public name: string | undefined;

    @Input()
    public label: string | undefined;

    @Input()
    public rows: number | undefined;

    @Input()
    public info: string | undefined;

    @Input()
    public maxlength: number | undefined;

    @Input()
    public required = false;

    // eslint-disable-next-line @angular-eslint/no-input-rename
    @Input('enforce-confidential')
    public enforceConfidential: boolean | undefined;

    private _textareaElement: HTMLElement | null = null;

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
                    if (this._textareaElement) {
                        synchronizeStatusChange(this._elementRef, this._textareaElement);
                    }
                });
            })
        );
        const el = this._elementRef.nativeElement as HTMLElement;
        this._textareaElement = el.querySelector('textarea');
        if (!this.required) {
            this.required = this.hasValidator(Validators.required);
        }
    }

    ngAfterViewInit(): void {
        if (this._textareaElement) {
            synchronizeStatusChange(this._elementRef, this._textareaElement);
        }
        this.setTextareaValue(this.value?.value);
        this._checkElement?.writeValue(this.value?.confidential);
    }

    ngOnDestroy(): void {
        this._subscription.unsubscribe();
    }

    public handleTextarea(event: Event): void {
        const value: string | null = (event.target as HTMLTextAreaElement).value;
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
        this.setTextareaValue(value?.value);
        this._checkElement?.writeValue(value?.confidential);
    }

    private setTextareaValue(value: string | undefined) {
        if (this._textareaElement) {
            const val = value ? value : '';
            this._renderer.setProperty(this._textareaElement, 'value', val);
        }
    }
}
