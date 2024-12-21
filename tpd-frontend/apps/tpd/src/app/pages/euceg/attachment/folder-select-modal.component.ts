import { Component } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { SvgIcons } from '@devacfr/bootstrap';
import { AttachmentService, FsElement, WalkTreeDirectory } from '@devacfr/euceg';
import { NotifierService } from '@devacfr/layout';
import _ from 'lodash-es';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

@Component({
    selector: 'app-folder-select-modal',
    templateUrl: './folder-select-modal.component.html',
    styleUrls: ['./folder-select-modal.component.scss'],
})
export class FolderSelectModalComponent {
    public onClose: () => void = _.noop;

    public onCancel: () => void = _.noop;

    public formControl: FormGroup;

    public directories$: Observable<WalkTreeDirectory>;

    public selectedPath: string | undefined;

    public currentItem: FsElement | undefined;

    constructor(
        public svgIcons: SvgIcons,
        public bsModalRef: BsModalRef,
        private _fb: FormBuilder,
        private _attachmentService: AttachmentService,
        private _notifierService: NotifierService
    ) {
        this.formControl = this._fb.group({});
        this.directories$ = this._attachmentService.getDirectories();
    }

    public close(): void {
        if (this.currentItem?.type === 'directory') {
            this._attachmentService
                .moveDirectoryTo(this.currentItem.path, this.selectedPath)
                .pipe(
                    finalize(() => {
                        this.bsModalRef.hide();
                        this.onClose();
                    })
                )
                .subscribe(() => {
                    this._notifierService.success(
                        `The Folder ${this.currentItem?.name} has been moved to folder ${this.selectedPath}.`
                    );
                });
        } else if (this.currentItem?.type === 'file') {
            this._attachmentService
                .moveFileTo(this.currentItem.uuid, this.selectedPath)
                .pipe(
                    finalize(() => {
                        this.bsModalRef.hide();
                        this.onClose();
                    })
                )
                .subscribe(() => {
                    this._notifierService.success(
                        `The File ${this.currentItem?.name} has been moved to folder ${this.selectedPath}.`
                    );
                });
        }
    }

    public cancel(): void {
        this.bsModalRef.hide();
        this.onCancel();
    }

    public handleChecked(event: Event, directory: WalkTreeDirectory) {
        const el = event.target as HTMLInputElement;
        this.selectedPath = el.checked ? directory.path : undefined;
    }

    public isDisabled(directory: WalkTreeDirectory) {
        return (
            this.currentItem &&
            this.currentItem.type === 'directory' &&
            directory.path.startsWith(this.currentItem.path)
        );
    }
}
