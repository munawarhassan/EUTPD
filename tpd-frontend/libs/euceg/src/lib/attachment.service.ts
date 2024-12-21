import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Hateoas, HateoasResponse, Page, Pageable } from '@devacfr/util';
import { Observable, of } from 'rxjs';
import { catchError, map, shareReplay } from 'rxjs/operators';
import { FsDirectory, FsElement, WalkTreeDirectory } from '.';
import { BACKEND_SERVER_API_URL_TOKEN } from '../shared';
import { AttachmentList } from './attachment-list.model';
import { AttachmentRequest } from './attachment-request.model';
import { AttachmentRevision } from './attachment-revision.model';
import { AttachmentUpdate } from './typing';

@Injectable({ providedIn: 'root' })
export class AttachmentService {
    private API_URL: string;
    constructor(
        @Inject(BACKEND_SERVER_API_URL_TOKEN) private BACKEND_SERVER_API_URL: string,
        private _httpClient: HttpClient
    ) {
        this.API_URL = this.BACKEND_SERVER_API_URL + 'attachments';
    }

    public page(pageable: Pageable): Observable<Page<AttachmentList>> {
        return this._httpClient
            .get<HateoasResponse<AttachmentList>>(this.API_URL, {
                params: pageable.httpParams(),
            })
            .pipe(Hateoas.page(this._httpClient, pageable));
    }

    public byFolder(pageable: Pageable, path?: string): Observable<Page<FsElement>> {
        let params = pageable.httpParams();
        if (path != null) {
            params = params.set('directory', path);
        }
        return this._httpClient
            .get<Page<FsElement>>(`${this.API_URL}/ByFolder`, {
                params: params,
            })
            .pipe(
                Page.mapOf(pageable),
                map((p) =>
                    p.map((fs) => {
                        fs.heathly$ = this.heathly(fs.name).pipe(shareReplay());
                        return fs;
                    })
                )
            );
    }

    public show(attachmentId: string): Observable<AttachmentRequest> {
        return this._httpClient
            .get<AttachmentRequest>(`${this.API_URL}/${attachmentId}`)
            .pipe(Hateoas.resource(this._httpClient));
    }

    public revisions(
        attachmentId: string,
        pageable: Pageable,
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        range?: { startDate?: Date; endDate?: Date }
    ): Observable<Page<AttachmentRevision>> {
        return this._httpClient
            .get<Page<AttachmentRevision>>(`${this.API_URL}/${attachmentId}/rev`, {
                params: pageable.httpParams(),
            })
            .pipe(Page.mapOf(pageable));
    }

    public latestRevision(id: string): Observable<AttachmentRevision> {
        return this._httpClient.get<AttachmentRevision>(`${this.API_URL}/${id}/rev/latest`);
    }

    public findAttachmentByFileName(filename: string): Observable<AttachmentRequest[]> {
        return this._httpClient.get<AttachmentRequest[]>(`${this.API_URL}/byFileName`, {
            params: {
                filename,
            },
        });
    }

    public heathly(filename: string): Observable<boolean> {
        return this._httpClient
            .get(`${this.API_URL}/healthy`, {
                params: {
                    filename,
                },
                observe: 'response',
            })
            .pipe(
                map((resp) => resp.status === 200),
                catchError(() => of(false))
            );
    }

    public fixIntegrity(filename: string): Observable<void> {
        return this._httpClient.post<void>(
            `${this.API_URL}/healthy`,
            {},
            {
                params: {
                    filename,
                },
            }
        );
    }

    public update(attachment: AttachmentUpdate): Observable<void> {
        return this._httpClient.put<void>(`${this.API_URL}/${attachment.attachmentId}`, attachment);
    }

    public delete(uuid: string): Observable<void> {
        return this._httpClient.delete<void>(`${this.API_URL}/${uuid}`);
    }

    public exists(filename): Observable<boolean> {
        return this._httpClient.get(`${this.API_URL}/exists/${filename}`, { observe: 'response' }).pipe(
            map((resp) => resp.status === 200),
            catchError(() => of(false))
        );
    }
    public moveDirectoryTo(path: string, newParentPath): Observable<FsDirectory> {
        return this._httpClient.post<FsDirectory>(`${this.API_URL}/moveDirectoryTo`, { path, newParentPath });
    }

    public moveFileTo(uuid: string, newParentPath): Observable<FsDirectory> {
        return this._httpClient.post<FsDirectory>(`${this.API_URL}/moveFileTo`, { uuid, newParentPath });
    }

    public createDirectory(directory: string): Observable<FsDirectory> {
        return this._httpClient.post<FsDirectory>(`${this.API_URL}/directories/${directory}`, null);
    }

    public deleteDirectory(directory: string): Observable<void> {
        return this._httpClient.delete<void>(`${this.API_URL}/directories/${directory}`);
    }

    public updateDirectory(directory: string, name: string): Observable<FsDirectory> {
        return this._httpClient.put<FsDirectory>(`${this.API_URL}/directories/${directory}`, { name });
    }

    public getDirectories(): Observable<WalkTreeDirectory> {
        return this._httpClient.get<WalkTreeDirectory>(`${this.API_URL}/directories`);
    }

    public upload$http(file: File, confidential: boolean): Observable<AttachmentRequest> {
        const formData = new FormData();
        formData.append('confidential', String(confidential));
        formData.append('file', file);
        return this._httpClient.post<AttachmentRequest>(`${this.API_URL}/upload`, formData);
    }
}
