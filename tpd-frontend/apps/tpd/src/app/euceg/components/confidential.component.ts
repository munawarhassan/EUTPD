import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    ElementRef,
    EventEmitter,
    HostBinding,
    HostListener,
    Input,
    OnChanges,
    OnInit,
    Optional,
    Output,
    Renderer2,
    Self,
    SimpleChanges,
} from '@angular/core';
import { NgControl, Validators } from '@angular/forms';
import { SvgIcons } from '@devacfr/bootstrap';
import { ValueAccessorBase } from '@devacfr/forms';
import { ClassBuilder } from '@devacfr/util';
import { uniqueId } from 'lodash-es';

@Component({
    selector: 'app-confidential',
    templateUrl: './confidential.component.html',
    styleUrls: ['./confidential.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConfidentialComponent extends ValueAccessorBase<boolean | undefined> implements OnInit, OnChanges {
    @HostBinding('class')
    public class = 'confidential-check';

    // eslint-disable-next-line @angular-eslint/no-input-rename
    @Input('enforce-confidential')
    public enforceConfidential: boolean | undefined;

    @Input()
    public name: string | undefined;

    @Input()
    public label: string | undefined;

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
    public required = false;

    public inputId: string;

    private _controlClass: string | undefined;

    private _text = '';

    @Output()
    public valueChange = new EventEmitter<boolean | undefined>();

    constructor(
        @Self() @Optional() ngControl: NgControl,
        private _elementRef: ElementRef<HTMLElement>,
        _renderer: Renderer2,
        public cd: ChangeDetectorRef,
        public svgIcons: SvgIcons
    ) {
        super(ngControl, _renderer);
        this.inputId = uniqueId('flexCheckbox');
        if (this.ngControl) {
            this.ngControl.valueAccessor = this;
        }
    }

    ngOnInit(): void {
        if (!this.required) {
            this.required = this.hasValidator(Validators.required);
        }
        this.updateTooltip();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (
            changes.enforceConfidential &&
            changes.enforceConfidential.currentValue != changes.enforceConfidential.previousValue
        ) {
            const el = this._elementRef.nativeElement;
            el.classList.toggle('enforce', this.enforceConfidential);
        }
    }

    public get text(): string {
        return this._text;
    }

    public override get value(): boolean | undefined {
        return super.value;
    }

    @Input()
    public override set value(value: boolean | undefined) {
        if (this.value !== value) {
            this.writeValue(value);
            this.markAsChanged();
            this.markAsTouched();
            this.valueChange.emit(this.value);
        }
    }

    @HostListener('click', ['$event'])
    protected onClick(event: Event) {
        event.preventDefault();
        event.stopPropagation();
        if (this.disabled) return;
        if (this.enforceConfidential) return;
        this.value = !this.value;
    }

    override writeValue(value: boolean | undefined) {
        if (this.enforceConfidential != null) {
            super.writeValue(this.enforceConfidential);
        } else {
            super.writeValue(value);
        }
        const el = this._elementRef.nativeElement;
        this.updateTooltip();
        el.classList.toggle('checked', this.value ?? false);
    }

    public get disabled(): boolean {
        return this._disabled;
    }

    @Input()
    public set disabled(value: boolean) {
        this.setDisabledState(value);
    }

    public override setDisabledState(isDisabled: boolean): void {
        super.setDisabledState(isDisabled);
        const el = this._elementRef.nativeElement;
        el.classList.toggle('disabled', isDisabled);
    }

    private updateTooltip(): void {
        if (this.value) {
            this._text = 'Confidential ' + (this.enforceConfidential ? '(readonly)' : '');
        } else {
            this._text = 'Not Confidential ' + (this.enforceConfidential ? '(readonly)' : '');
        }
        this.cd.markForCheck();
    }
}
