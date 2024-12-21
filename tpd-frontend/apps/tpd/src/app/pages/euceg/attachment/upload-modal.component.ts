import { Component } from '@angular/core';
import _ from 'lodash-es';
import { FileItem, FileUploader } from 'ng2-file-upload';
import { BsModalRef } from 'ngx-bootstrap/modal';

@Component({
    selector: 'app-upload-modal',
    templateUrl: './upload-modal.component.html',
    styleUrls: ['./upload-modal.component.scss'],
})
export class UploadModalComponent {
    public onClose: () => void = _.noop;

    public uploader!: FileUploader;

    constructor(public bsModalRef: BsModalRef) {}

    public close(): void {
        this.bsModalRef.hide();
        this.onClose();
    }

    public replace(fileItem: FileItem) {
        const file = fileItem as FileItem & { exists?: boolean };
        delete file.exists;
        fileItem.upload();
    }

    public handleQueueChanged() {
        if (this.uploader.queue.length == 0) {
            this.close();
        }
    }
}
