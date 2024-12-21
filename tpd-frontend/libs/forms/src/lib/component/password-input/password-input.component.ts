import { Component, ElementRef, Input, Renderer2, Self, ViewChild } from '@angular/core';
import { ControlValueAccessor, NgControl } from '@angular/forms';
import { ClassBuilder } from '@devacfr/util';

export interface IPasswordMeterOptions {
    checkUppercase: boolean;
    checkLowercase: boolean;
    checkDigit: boolean;
    checkChar: boolean;
    scoreHighlightClass: string;
}

const DefaultPasswordMeterOptions = {
    checkUppercase: true,
    checkLowercase: true,
    checkDigit: true,
    checkChar: true,
    scoreHighlightClass: 'active',
};

@Component({
    selector: 'lt-password-input',
    exportAs: 'ltPasswordInput',
    templateUrl: 'password-input.component.html',
})
export class PasswordInputComponent implements ControlValueAccessor {
    @Input()
    public minlength = 5;

    @Input()
    public required = true;

    @Input()
    public get controlClass(): string {
        const builder = ClassBuilder.create('form-control');
        if (this._controlClass) builder.css(this._controlClass);
        return builder.toString();
    }
    public set controlClass(value: string | null) {
        this._controlClass = value;
    }

    @Input()
    public placeholder: string | null = null;

    @ViewChild('Input', { static: true })
    private _inputElement: ElementRef | undefined;

    @ViewChild('Highlight', { static: true })
    private _highlightElement: ElementRef | undefined;

    private _options: IPasswordMeterOptions;
    private _score = 0;
    private _checkSteps = 5;
    private _controlClass: string | null = null;

    private _innerValue: string | undefined;

    private _disabled = false;

    // eslint-disable-next-line @typescript-eslint/no-empty-function
    private _onChange = (value: any) => {};

    // eslint-disable-next-line @typescript-eslint/no-empty-function
    private _onTouched = () => {};

    constructor(@Self() public ngControl: NgControl, private _renderer: Renderer2) {
        this._options = Object.assign({}, DefaultPasswordMeterOptions);
        this.ngControl.valueAccessor = this;
    }

    public get value(): string | undefined {
        return this._innerValue;
    }

    public set value(value: string | undefined) {
        if (this._innerValue !== value) {
            this._innerValue = value;
            this.markAsChanged();
            this.markAsTouched();
        }
    }

    public get disabled(): boolean {
        return this._disabled;
    }

    public setDisabledState?(isDisabled: boolean): void {
        this._disabled = isDisabled;
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    public registerOnChange(fn: any): void {
        this._onChange = fn;
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    public registerOnTouched(fn: any): void {
        this._onTouched = fn;
    }

    public writeValue(value: string) {
        this._innerValue = value;
    }

    public handleInput(event: Event): void {
        const value: string | null = (event.target as HTMLInputElement).value;
        this._onChange(value);
        this.check();
    }

    public markAsTouched() {
        this._onTouched();
    }

    public markAsChanged() {
        this._onChange(this.value);
    }
    public get inputElement(): HTMLInputElement | undefined {
        return this._inputElement?.nativeElement as HTMLInputElement;
    }
    public get highlightElement(): HTMLInputElement | undefined {
        return this._highlightElement?.nativeElement as HTMLInputElement;
    }

    public reset() {
        this._score = 0;
        this.highlight();
    }

    public getScore() {
        return this._score;
    }

    public check() {
        let score = 0;
        const checkScore = this.getCheckScore();
        if (this.checkLength()) {
            score = score + checkScore;
        }

        if (this._options.checkUppercase && this.checkLowerCase()) {
            score = score + checkScore;
        }

        if (this._options.checkLowercase && this.checkUppercase()) {
            score = score + checkScore;
        }

        if (this._options.checkDigit && this.checkDigit()) {
            score = score + checkScore;
        }

        if (this._options.checkChar && this.checkChar()) {
            score = score + checkScore;
        }

        this._score = score;
        this.highlight();
    }

    private getCheckScore(): number {
        let count = 1;
        if (this._options.checkUppercase) {
            count++;
        }

        if (this._options.checkLowercase) {
            count++;
        }

        if (this._options.checkDigit) {
            count++;
        }

        if (this._options.checkChar) {
            count++;
        }

        this._checkSteps = count;
        return 100 / this._checkSteps;
    }

    public visibility(event: Event): void {
        const visibilityElement = event.currentTarget as HTMLElement;
        if (visibilityElement && this.inputElement) {
            const visibleIcon = visibilityElement.querySelector('i:not(.d-none), .svg-icon:not(.d-none)');

            const hiddenIcon = visibilityElement.querySelector('i.d-none, .svg-icon.d-none');

            const typeAttr = this.inputElement.getAttribute('type') || '';

            if (typeAttr === 'password') {
                this.inputElement.setAttribute('type', 'text');
            } else {
                this.inputElement.setAttribute('type', 'password');
            }

            visibleIcon?.classList.add('d-none');
            hiddenIcon?.classList.remove('d-none');

            this.inputElement.focus();
        }
    }

    private highlight() {
        const items = this.highlightElement ? [].slice.call(this.highlightElement.querySelectorAll('div')) : [];
        const total = items.length;
        let index = 0;
        const checkScore = this.getCheckScore();
        const score = this.getScore();

        items.map((item: HTMLElement) => {
            index++;
            if (checkScore * index * (this._checkSteps / total) <= score) {
                item.classList.add('active');
            } else {
                item.classList.remove('active');
            }
        });
    }

    private checkLength(): boolean {
        if (this.inputElement) {
            return this.inputElement.value.length >= this.minlength; // 20 score
        }

        return false;
    }

    private checkLowerCase(): boolean {
        const val = this.inputElement ? this.inputElement.value : '';
        return /[a-z]/.test(val); // 20 score
    }

    private checkUppercase(): boolean {
        const val = this.inputElement ? this.inputElement.value : '';
        return /[A-Z]/.test(val); // 20 score
    }

    private checkDigit(): boolean {
        const val = this.inputElement ? this.inputElement.value : '';
        return /[0-9]/.test(val); // 20 score
    }

    private checkChar(): boolean {
        const val = this.inputElement ? this.inputElement.value : '';
        // eslint-disable-next-line no-useless-escape
        return /[~`!#$%\^&*+=\-\[\]\\';,/{}|\\":<>\?]/g.test(val); // 20 score
    }
}
