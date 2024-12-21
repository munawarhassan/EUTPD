import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { UserToken } from '@devacfr/auth';
import { AttachmentList, AttachmentRequest, AttachmentService, FsDirectory } from '@devacfr/euceg';
import { I18nService, NotifierService } from '@devacfr/layout';
import { FileUploader } from 'ng2-file-upload';
import { from, Observable } from 'rxjs';
import Swal from 'sweetalert2';

@Injectable({ providedIn: 'root' })
export class AttachmentManager {
    constructor(
        private _httpClient: HttpClient,
        private _i8n: I18nService,
        private _attachmentService: AttachmentService,
        private _notifierService: NotifierService,
        private _userToken: UserToken
    ) {}

    public deleteAttachement(
        attachment: AttachmentList | AttachmentRequest | { filename: string; attachmentId: string }
    ): Observable<boolean> {
        return from(
            new Promise<boolean>((resolve, reject): void => {
                Swal.fire({
                    title: 'Are you sure?',
                    html: 'Are you sure that you want to delete ' + attachment.filename + '?',
                    icon: 'warning',
                    showCancelButton: true,
                    confirmButtonText: this._i8n.instant('global.button.delete'),
                    cancelButtonText: this._i8n.instant('global.button.cancel'),
                }).then((result) => {
                    if (result.isConfirmed) {
                        this._attachmentService.delete(attachment.attachmentId).subscribe({
                            next: () => {
                                this._notifierService.success(
                                    'The attachment ' + attachment.filename + ' has been removed.'
                                );
                                resolve(true);
                            },
                            error: (err) => {
                                this._notifierService.error(err);
                                reject(err);
                            },
                        });
                    } else {
                        resolve(false);
                    }
                });
            })
        );
    }

    public deleteDirectory(directory: FsDirectory): Observable<boolean> {
        return from(
            new Promise<boolean>((resolve, reject): void => {
                Swal.fire({
                    title: 'Are you sure?',
                    html: 'Are you sure that you want to delete the Folder ' + directory.name + '?',
                    icon: 'warning',
                    showCancelButton: true,
                    confirmButtonText: this._i8n.instant('global.button.delete'),
                    cancelButtonText: this._i8n.instant('global.button.cancel'),
                }).then((result) => {
                    if (result.isConfirmed) {
                        this._attachmentService.deleteDirectory(directory.path).subscribe({
                            next: () => {
                                this._notifierService.success('The Folder ' + directory.name + ' has been removed.');
                                resolve(true);
                            },
                            error: (err) => {
                                this._notifierService.error(err);
                                reject(err);
                            },
                        });
                    } else {
                        resolve(false);
                    }
                });
            })
        );
    }

    public preview(uuid: string) {
        const url = BACKEND_SERVER_API_URL + `attachments/download?uuid=${uuid}`;

        this._httpClient.get(url, { responseType: 'blob' }).subscribe((blob) => {
            const aTag = document.createElement('a');
            aTag.rel = 'noopener';
            aTag.target = '_blank';
            aTag.href = URL.createObjectURL(blob);
            aTag.click();
        });
    }

    public createUploader(): FileUploader {
        const uploader = new FileUploader({
            allowedMimeType: ['application/pdf'],
            allowedFileType: ['pdf'],
            removeAfterUpload: true,
            authToken: 'Bearer ' + this._userToken.currentUser?.token,
            url: BACKEND_SERVER_API_URL + 'attachments/upload',
        });
        return uploader;
    }
}
