import { AfterViewInit, Component, ElementRef, Input, OnInit, Renderer2, Self, ViewChild } from '@angular/core';
import { NgControl, Validators } from '@angular/forms';
import { uniqueId } from 'lodash-es';
import { ValueAccessorBase } from './value-accessor-base';

@Component({
    // eslint-disable-next-line @angular-eslint/component-selector
    selector: 'form-file',
    templateUrl: './form-file.component.html',
})
export class FormFileComponent extends ValueAccessorBase<FileList | undefined> implements OnInit, AfterViewInit {
    @Input()
    public label: string | undefined;

    @Input()
    public name: string | undefined;

    @Input()
    public info: string | undefined;

    @Input()
    public helpText: string | undefined;

    @Input()
    public required: boolean | undefined;

    @Input()
    public multiple = false;

    @Input()
    public accept: string | undefined;

    public inputId = uniqueId('formFileMultiple');

    @ViewChild('InputFile')
    private _inputFile: ElementRef | undefined;

    constructor(@Self() ngControl: NgControl, _renderer: Renderer2) {
        super(ngControl, _renderer);
        this.ngControl.valueAccessor = this;
    }

    ngOnInit(): void {
        if (!this.required) {
            this.required = this.hasValidator(Validators.required);
        }
    }

    ngAfterViewInit(): void {
        if (this.multiple) this._renderer.setAttribute(this._inputFile?.nativeElement, 'multiple', '');
    }

    public handleFileChange(event: Event) {
        const input = event.target as HTMLInputElement;
        const files = input.files;
        this.value = files?.length ? files : undefined;
    }
}
