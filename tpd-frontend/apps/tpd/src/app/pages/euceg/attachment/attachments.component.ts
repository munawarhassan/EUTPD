import { ChangeDetectorRef, Component, OnDestroy, TemplateRef, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PermissionsService, UserToken } from '@devacfr/auth';
import { BlockUI, DrawerDirective, SvgIcons } from '@devacfr/bootstrap';
import { Channels } from '@devacfr/core';
import { AttachmentList, AttachmentService, EucegService, FsDirectory, FsElement } from '@devacfr/euceg';
import { I18nService, NotifierService, TableOptions } from '@devacfr/layout';
import { PageObserver, Pageable } from '@devacfr/util';
import { SearchComponent } from '@tpd/app/components/search';
import { isArray } from 'lodash-es';
import { FileItem, FileUploader } from 'ng2-file-upload';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Observable, Subscription } from 'rxjs';
import { finalize, switchMap, tap } from 'rxjs/operators';
import { AttachmentManager } from './attachment.manager';
import { FolderSelectModalComponent } from './folder-select-modal.component';
import { UploadModalComponent } from './upload-modal.component';

@Component({
    selector: 'app-attachments',
    templateUrl: './attachments.component.html',
    styleUrls: ['./attachments.component.scss'],
})
export class AttachmentsComponent implements OnDestroy {
    public tableOptions: TableOptions = {
        pagination: 'infinite',
        scrollTrack: 'vertical',
        columns: [
            {
                name: 'name',
                title: 'Name',
                sort: {
                    ignoreCase: true,
                },
                class: 'min-w-350px',
            },
            {
                name: 'confidential',
                sort: false,
                align: 'center',
                i18n: 'attachments.list.fields.confidential',
                class: 'min-w-100px d-none d-xl-table-cell',
            },
            {
                name: 'status',
                sort: false,
                i18n: 'attachments.list.fields.status',
                align: 'center',
                class: 'min-w-50px d-none d-xl-table-cell',
            },
            {
                name: 'size',
                sort: false,
                i18n: 'attachments.list.fields.size',
                class: 'min-w-70px d-none d-xl-table-cell',
            },
            {
                name: 'lastModifiedDate',
                sort: false,
                title: 'LAST MODIFIED',
                class: 'min-w-100px d-none d-xl-table-cell',
            },
            {
                name: 'action',
                sort: false,
                title: '',
                align: 'center',
                class: 'min-w-100px d-none d-xl-table-cell',
            },
        ],
    };

    public channels = [Channels.ATTACHMENT];

    public page: PageObserver<FsElement>;
    public currentPageable: Pageable;
    public totalItems = 0;

    public uploader: FileUploader;
    public hasBaseDropZoneOver = false;

    public selectedWhereUsed: AttachmentList | undefined;

    @ViewChild(SearchComponent, { static: true })
    private searchComponent!: SearchComponent;

    private _subscriptions = new Subscription();
    private _block = new BlockUI('#m_portlet_attachments');
    private _uploadModal: BsModalRef<UploadModalComponent> | undefined;

    public _path: string[] = [];

    public modalRef?: BsModalRef;
    public folderForm: FormGroup;
    public searchEnable = false;

    constructor(
        public svgIcons: SvgIcons,
        public userToken: UserToken,
        public permissionService: PermissionsService,
        public euceg: EucegService,
        private _modalService: BsModalService,
        private _attachmentService: AttachmentService,
        private _attachmentManager: AttachmentManager,
        private _notifierService: NotifierService,
        private _i8n: I18nService,
        private _cd: ChangeDetectorRef,
        private _fb: FormBuilder
    ) {
        this.folderForm = this._fb.group({
            path: [null],
            name: [null, [Validators.required, Validators.pattern('^[\\w-]*$')]],
        });

        this.uploader = this.createUploader();

        this.currentPageable = Pageable.of(0, 20).order().set('name', 'ASC', true).end();
        this.page = (obs: Observable<Pageable>) => {
            return obs.pipe(
                tap(() => {
                    this._block.block();
                }),
                switchMap((pageable) => {
                    return this._attachmentService
                        .byFolder(pageable, this.searchEnable ? undefined : this.getPath())
                        .pipe(
                            tap((page) => (this.totalItems = page.totalElements)),
                            finalize(() => this._block.release())
                        );
                }),
                this._notifierService.catchError()
            );
        };
    }

    public ngOnDestroy(): void {
        this._subscriptions.unsubscribe();
    }

    public get path(): string[] {
        return this._path;
    }

    public getPath(): string {
        return this._path.join('/');
    }

    public setPath(path: string | string[]) {
        this._path = isArray(path) ? path : this.splitPath(path);
        this.resetFilter();
        this.refresh();
    }

    public setWhereUsed(attachment: AttachmentList, drawer: DrawerDirective) {
        drawer.toggle();
        this.selectedWhereUsed = attachment;
    }

    public splitPath(path: string): string[] {
        if (path.length === 0) {
            return [];
        }
        const ar = path.split('/');
        return ar;
    }

    public refresh(): void {
        this.currentPageable = this.currentPageable.first();
    }

    private refreshOnCurrenPage() {
        this.currentPageable = this.currentPageable.current();
        this._cd.detectChanges();
    }

    public handleFileOverBase(over: boolean): void {
        this.hasBaseDropZoneOver = over;
    }

    public handleFileDrop() {
        this.openUploadModal();
    }

    public search(searchTerm: string | undefined) {
        this.currentPageable.clearFilter();
        if (searchTerm && searchTerm.length > 0) {
            this.currentPageable.filter().contains('name', searchTerm);
            this.searchEnable = true;
        } else {
            this.searchEnable = false;
        }
        this.refresh();
    }

    public resetFilter() {
        this.searchComponent.clear();
        this.searchEnable = false;
        this.currentPageable.clearFilter();
    }

    public preview(attachment: AttachmentList) {
        this._attachmentManager.preview(attachment.attachmentId);
    }

    public deleteAttachement(attachment: AttachmentList) {
        this._attachmentManager.deleteAttachement(attachment).subscribe((done) => {
            if (done) this.refresh();
        });
    }

    public deletePhysicalFile(item: FsElement) {
        if (item.type === 'file') {
            this._attachmentManager
                .deleteAttachement({ filename: item.name, attachmentId: item.uuid })
                .subscribe((done) => {
                    if (done) this.refresh();
                });
        }
    }

    public openUploadModal() {
        this._uploadModal = this._modalService.show(UploadModalComponent, {
            animated: true,
            backdrop: true,
            initialState: {
                uploader: this.uploader,
            },
            class: 'modal-lg',
        });
        this._subscriptions.add(
            this._uploadModal.onHidden?.subscribe(() => {
                this.uploader.clearQueue();
                this._uploadModal = undefined;
                this.refresh();
            })
        );
    }

    public openSelectFolderModal(currentItem: FsElement) {
        this._modalService.show(FolderSelectModalComponent, {
            animated: true,
            backdrop: true,
            initialState: {
                selectedPath: currentItem.parentPath,
                currentItem,
                onClose: () => {
                    this.refreshOnCurrenPage();
                },
            },
        });
    }

    public openModal(template: TemplateRef<unknown>, patch?: { path?: string; name: string }) {
        this.folderForm.reset();
        if (patch) {
            this.folderForm.patchValue(patch);
        }
        this.modalRef = this._modalService.show(template, {
            class: 'modal-dialog-centered',
        });
    }

    public addNewFolder() {
        if (this.folderForm.invalid) {
            return;
        }
        this.modalRef?.hide();
        const newFolder = [...this.path, this.folderForm.value.name].join('/');
        this._attachmentService.createDirectory(newFolder).subscribe({
            next: () => {
                this.refreshOnCurrenPage();
            },
            error: (err) => this._notifierService.error(err),
        });
    }

    public renameFolder() {
        if (this.folderForm.invalid) {
            return;
        }
        this.modalRef?.hide();
        const folder = this.folderForm.value;
        this._attachmentService.updateDirectory(folder.path, folder.name).subscribe({
            next: () => {
                this.refreshOnCurrenPage();
            },
            error: (err) => this._notifierService.error(err),
        });
    }

    public deleteDirectory(directory: FsDirectory) {
        this._attachmentManager.deleteDirectory(directory).subscribe({
            next: (done) => {
                if (done) this.refresh();
            },
            error: (err) => this._notifierService.error(err),
        });
    }

    public fixIntegrity(filename: string) {
        this._attachmentService.fixIntegrity(filename).subscribe({
            next: () => {
                this._notifierService.success(`The file ${filename} has been fixed`);
                this.refresh();
            },
            error: (err) => this._notifierService.error(err),
        });
    }

    private createUploader(): FileUploader {
        const uploader = this._attachmentManager.createUploader();

        uploader.onProgressItem = () => {
            this._cd.detectChanges();
        };

        uploader.onErrorItem = (item: FileItem) => {
            // eslint-disable-next-line max-len
            this._notifierService.error(
                `Error When try Upload file '${item._file.name}'. The File type '${item._file.type}' is maybe not accepted.`
            );
        };

        uploader.onCompleteItem = () => {
            this._cd.detectChanges();
        };
        uploader.onAfterAddingFile = (fileItem: FileItem) => {
            const file = fileItem as FileItem & { exists: boolean };
            file.exists = false;
            this._attachmentService.exists(fileItem._file.name).subscribe((exists) => {
                file.exists = exists;
                if (!exists) {
                    fileItem.upload();
                }
                this._cd.detectChanges();
            });
        };
        return uploader;
    }
}
