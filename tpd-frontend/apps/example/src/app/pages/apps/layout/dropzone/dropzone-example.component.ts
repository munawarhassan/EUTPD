import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { FileUploader } from 'ng2-file-upload';

@Component({
    selector: 'app-dropzone-example',
    templateUrl: 'dropzone-example.component.html',
})
export class DropzoneExampleComponent {
    public uploader: FileUploader;

    formGroup = this.fb.group({
        fullname: [null],
        email: [null],
        message: [null],
        file: [null, Validators.required],
    });

    constructor(private fb: FormBuilder) {
        this.uploader = this.createImportFileUploader();
    }

    public createImportFileUploader(): FileUploader {
        return new FileUploader({
            removeAfterUpload: false,
            itemAlias: 'files',
            url: 'import',
        });
    }

    public submit(): void {
        if (this.formGroup.invalid) return;
    }
}
