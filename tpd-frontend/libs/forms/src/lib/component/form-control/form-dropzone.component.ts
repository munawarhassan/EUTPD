import {
    AfterViewInit,
    Component,
    ElementRef,
    EventEmitter,
    Input,
    OnChanges,
    OnInit,
    Optional,
    Output,
    Renderer2,
    Self,
    SimpleChanges,
    TemplateRef,
    ViewChild,
} from '@angular/core';
import { NgControl, Validators } from '@angular/forms';
import { ErrorResponse } from '@devacfr/util';
import { isArray } from 'lodash-es';
import { FileItem, FileUploader, ParsedResponseHeaders } from 'ng2-file-upload';
import { ValueAccessorBase } from './value-accessor-base';

@Component({
    // eslint-disable-next-line @angular-eslint/component-selector
    selector: 'form-dropzone',
    templateUrl: './form-dropzone.component.html',
    styleUrls: ['./form-dropzone.component.scss'],
})
export class FormDropzoneComponent
    extends ValueAccessorBase<FileItem[] | undefined>
    implements OnInit, AfterViewInit, OnChanges
{
    @Input()
    public inputName = 'file';

    @Input()
    public label: string | undefined;

    @Input()
    public helpText: string | undefined;

    @Input()
    public multiple = false;

    @Input()
    public uploader: FileUploader | undefined;

    @Input()
    public required = false;

    @Output()
    public errorItem = new EventEmitter<{ fileItem: FileItem; response: string }>();

    @Output()
    public removeFile = new EventEmitter<{ fileItem: FileItem }>();

    @Output()
    public removeAllFile = new EventEmitter<unknown>();

    @Output()
    public queueChanged = new EventEmitter<void>();

    @Input()
    public itemTemplate:
        | TemplateRef<{ fileItem: FileItem; uploader: FileUploader; control: FormDropzoneComponent }>
        | undefined;

    public hasDropZoneOver = false;
    public errorResponse: ErrorResponse | undefined;

    @ViewChild('InputFile')
    private _inputFile: ElementRef | undefined;

    constructor(@Self() @Optional() ngControl: NgControl, private _elementRef: ElementRef, _renderer: Renderer2) {
        super(ngControl, _renderer);
        if (this.ngControl) {
            this.ngControl.valueAccessor = this;
        }
    }

    ngOnInit(): void {
        if (!this.required) {
            this.required = this.hasValidator(Validators.required);
        }
    }

    ngAfterViewInit(): void {
        if (this.multiple) this._renderer.setAttribute(this._inputFile?.nativeElement, 'multiple', '');
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (this.uploader && changes.uploader && changes.uploader.currentValue) {
            const onErrorItem = this.uploader.onErrorItem;
            this.uploader.onErrorItem = (
                fileItem: FileItem,
                response: string,
                status: number,
                headers: ParsedResponseHeaders
            ) => {
                this.errorResponse = JSON.parse(response);
                if (onErrorItem) {
                    onErrorItem(fileItem, response, status, headers);
                }
                this.errorItem.emit({ fileItem, response });
            };
            const removeFromQueue = this.uploader.removeFromQueue;
            this.uploader.removeFromQueue = (fileItem: FileItem) => {
                removeFromQueue.call(this.uploader, fileItem);
                this.removeFile.emit({ fileItem });
                this.queueChanged.emit();
                this._updateValueFromUploader();
            };
            const onAfterAddingFile = this.uploader.onAfterAddingFile;
            this.uploader.onAfterAddingFile = (file: FileItem) => {
                this.onAddFile(file);
                if (onAfterAddingFile) {
                    onAfterAddingFile(file);
                }
            };
        }
    }

    public openFileDialog(): void {
        const input = this._inputFile?.nativeElement as HTMLInputElement;
        if (input) {
            input.click();
        }
    }

    public fileOverBase(e: boolean): void {
        this.hasDropZoneOver = e;
    }

    public onAddFile(fileItem: FileItem) {
        this._updateValueFromUploader();
        this.queueChanged.emit();
    }

    public handleRemoveFile(fileItem: FileItem) {
        fileItem.remove();
    }

    public handleRemoveAllFiles() {
        if (!this.uploader) return;
        while (this.uploader.queue.length) {
            const fileItem = this.uploader.queue[0];
            fileItem.remove();
        }
        this.removeAllFile.emit();
    }

    public hasErrorResponse(fileItem: FileItem): boolean {
        return this.getErrorResponse(fileItem) != null;
    }

    public getErrorResponse(fileItem: FileItem): string[] | undefined {
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const item = fileItem as any;
        const errorResponse = item['errorResponse'];
        if (errorResponse != null) {
            return isArray(errorResponse) ? errorResponse : [errorResponse];
        }
        return undefined;
    }

    private _updateValueFromUploader(): void {
        if (this.uploader) {
            this.value = this.uploader.queue.length > 0 ? this.uploader?.queue : undefined;
        }
    }
}
