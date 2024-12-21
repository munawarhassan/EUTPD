import {
    ChangeDetectorRef,
    Component,
    ElementRef,
    EventEmitter,
    HostBinding,
    HostListener,
    Input,
    Optional,
    Output,
    Renderer2,
    Self,
    ViewChild,
} from '@angular/core';
import { ControlValueAccessor, NgControl } from '@angular/forms';
import { ClassBuilder } from '@devacfr/util';
import _ from 'lodash-es';

const BlankUrl = './assets/media/avatars/blank.png';
@Component({
    selector: 'lt-image-input',
    templateUrl: 'image-input.component.html',
})
export class ImageInputComponent implements ControlValueAccessor {
    @Input()
    public get image(): string | ArrayBuffer | null {
        return this._image;
    }

    public set image(value: string | ArrayBuffer | null) {
        this._image = value;
        this._renderer.setStyle(
            this._element.nativeElement,
            'background-image',
            this._image ? `url(${this._image})` : `url(${BlankUrl})`
        );
    }

    @Input()
    public cancellable = true;

    @Input()
    public inputName = 'file';

    @Input()
    public disabled = false;

    @Input()
    public mode: 'square' | 'circle' | 'outline' | undefined;

    @HostBinding('class')
    public get class() {
        const css = ClassBuilder.create('image-input');
        if (!this.image && !this.storedImage) css.css('image-input-empty');
        if (this.storedImage) css.css('image-input-changed');
        if (this.mode === 'circle') css.css('image-input-circle');
        if (this.mode === 'outline') css.css('image-input-outline');
        if (this.disabled) css.css('image-input-disabled');
        return css.toString();
    }

    @Output()
    public changed: EventEmitter<File> = new EventEmitter<File>(true);
    @Output()
    public removed: EventEmitter<void> = new EventEmitter<void>(true);

    @Output()
    public cancelled: EventEmitter<void> = new EventEmitter<void>(true);

    public get storedImage(): string | ArrayBuffer | null {
        return this._storedImage;
    }

    public set storedImage(value: string | ArrayBuffer | null) {
        this._storedImage = value;
        if (this._storedImage) {
            this._renderer.setStyle(this.wrapperElement, 'background-image', `url('${this._storedImage}')`);
        } else {
            this._renderer.setStyle(this.wrapperElement, 'background-image', 'none');
        }
    }

    protected _onChange = _.noop;

    protected _onTouched = _.noop;

    @ViewChild('input', { static: true })
    private _inputElement: ElementRef | undefined;

    @ViewChild('wrapper', { static: true })
    private _wrapperElement: ElementRef | undefined;

    public _image: string | ArrayBuffer | null = BlankUrl;
    public _storedImage: string | ArrayBuffer | null = null;

    constructor(
        @Self() @Optional() public ngControl: NgControl,
        private _element: ElementRef,
        private _renderer: Renderer2,
        private _cd: ChangeDetectorRef
    ) {
        if (this.ngControl) {
            this.ngControl.valueAccessor = this;
        }
    }

    writeValue(value: string | ArrayBuffer | null): void {
        this.image = value;
        this.storedImage = null;
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    registerOnChange(fn: any): void {
        this._onChange = fn;
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    registerOnTouched(fn: any): void {
        this._onTouched = fn;
    }

    setDisabledState(state: boolean) {
        this.disabled = state;
    }

    public get element(): HTMLElement | undefined {
        return this._element?.nativeElement as HTMLInputElement;
    }

    public get inputElement(): HTMLInputElement | undefined {
        return this._inputElement?.nativeElement as HTMLInputElement;
    }

    public get wrapperElement(): HTMLElement | undefined {
        return this._wrapperElement?.nativeElement as HTMLInputElement;
    }

    @HostListener('change', ['$event'])
    public handleChange(event: Event): void {
        event.preventDefault();

        if (this.inputElement != null && this.inputElement.files && this.inputElement.files[0]) {
            const reader = new FileReader();

            reader.onload = (evt: ProgressEvent<FileReader>) => {
                if (!evt.target) {
                    return;
                }
                this.storedImage = evt.target.result;
                if (this.inputElement != null && this.inputElement.files) {
                    this.changed.emit(this.inputElement.files[0]);
                }
                this._cd.detectChanges();
            };

            reader.readAsDataURL(this.inputElement.files[0]);
            this.markAsTouched();
            this.markAsChanged();
        }
    }

    public cancel(event?: Event): void {
        if (event) {
            event.preventDefault();
        }
        this.storedImage = null;
        if (this.inputElement) {
            this.inputElement.value = '';
        }
        this.markAsTouched();
        this.markAsChanged();
        this.cancelled.emit();
    }

    public remove(event?: Event) {
        if (event) {
            event.preventDefault();
        }
        this.storedImage = null;
        this.image = null;
        if (this.inputElement) {
            this.inputElement.value = '';
        }
        this.markAsTouched();
        this.markAsChanged();
        this.removed.emit();
    }

    public complete(): void {
        this.image = this.storedImage;
        this.storedImage = null;
        this.markAsTouched();
    }

    public markAsTouched() {
        this._onTouched();
    }

    public markAsChanged() {
        if (this.inputElement?.files != null) {
            this._onChange(this.inputElement?.files[0]);
        }
    }
}
