import { Directive, ElementRef, EventEmitter, HostListener, Input, Output } from '@angular/core';
import { FileUploaderOptions, FileUploader } from 'ng2-file-upload';

function isDescendant(parent: Element, child: Element | null): boolean {
    if (child == null) return false;
    if (child === parent) return false;
    let node = child.parentNode;
    while (node != null) {
        if (node === parent) {
            return true;
        }
        node = node.parentNode;
    }
    return false;
}

/**
 * Modification of ng2FileDrop directive to fix the Flickering
 * Issue: https://github.com/valor-software/ng2-file-upload/issues/1190
 */
@Directive({ selector: '[ltFileDrop]' })
export class FileDropDirective {
    @Input() uploader?: FileUploader;
    @Output() fileOver: EventEmitter<boolean> = new EventEmitter();
    @Output() fileDrop: EventEmitter<FileList> = new EventEmitter<FileList>();

    public isDraggingOverDropZone = false;

    constructor(private _elementRef: ElementRef) {}

    public getOptions(): FileUploaderOptions | undefined {
        return this.uploader?.options;
    }

    public getFilters(): string {
        return '';
    }

    @HostListener('drop', ['$event'])
    protected onDrop(event: DragEvent): void {
        this.isDraggingOverDropZone = false;
        this.preventAndStop(event);
        if (event.dataTransfer) {
            const dataTransfer = event.dataTransfer;
            const options = this.getOptions();
            const filters = this.getFilters();
            if (options) {
                const files: File[] = [];
                if (dataTransfer.items) {
                    for (let index = 0; index < dataTransfer.items.length; index++) {
                        if (dataTransfer.items[index].kind === 'file') {
                            const element = dataTransfer.items[index].getAsFile();
                            if (element) files.push(element);
                        }
                    }
                } else {
                    for (let index = 0; index < dataTransfer.files.length; index++) {
                        const element = dataTransfer.files[index];
                        if (element) files.push(element);
                    }
                }
                this.uploader?.addToQueue(files, options, filters);
                this.fileDrop.emit(dataTransfer.files);
            }
        }
        this.fileOver.emit(this.isDraggingOverDropZone);
    }

    @HostListener('dragover', ['$event'])
    protected onDragOver(event: DragEvent): void {
        if (event.dataTransfer) {
            const dataTransfer = event.dataTransfer;
            if (!this.haveFiles(event)) {
                dataTransfer.dropEffect = 'none';
                return;
            } else if (!this.isDraggingOverDropZone) {
                this.isDraggingOverDropZone = true;
                this.fileOver.emit(this.isDraggingOverDropZone);
                dataTransfer.dropEffect = 'copy';
            }
        }
        this.preventAndStop(event);
    }

    @HostListener('dragleave', ['$event'])
    protected onDragLeave(event: MouseEvent): void {
        const el = this._elementRef.nativeElement as Element;
        if (isDescendant(el, event.target as Element)) {
            return;
        }
        if (this.isDraggingOverDropZone) {
            this.isDraggingOverDropZone = false;
            this.fileOver.emit(this.isDraggingOverDropZone);
        }
        // this.preventAndStop(event);
    }

    protected preventAndStop(event: DragEvent): void {
        // If there are no files, we don't want to stop
        // propagation so we don't interfere with other
        // drag and drop behaviour.
        if (!this.haveFiles(event)) return;
        event.stopPropagation();
        event.preventDefault();
    }

    protected haveFiles(event: DragEvent) {
        if (event.dataTransfer?.items) {
            for (let index = 0; index < event.dataTransfer.items.length; index++) {
                if (event.dataTransfer.items[index].kind === 'file') {
                    return true;
                }
            }
        } else if (event.dataTransfer?.types) {
            // Because e.dataTransfer.types is an Object in
            // IE, we need to iterate like this instead of
            // using e.dataTransfer.types.some()
            for (let i = 0; i < event.dataTransfer.types.length; i++) {
                if (event.dataTransfer.types[i] === 'Files') return true;
            }
        } else if (event.dataTransfer?.files) {
            return event.dataTransfer.files.length > 0;
        }
        return false;
    }
}
