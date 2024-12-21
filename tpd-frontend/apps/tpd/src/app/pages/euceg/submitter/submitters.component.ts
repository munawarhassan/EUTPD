import { ChangeDetectionStrategy, ChangeDetectorRef, Component, ElementRef, OnDestroy, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthProvider } from '@devacfr/auth';
import { BlockUI, SvgIcons } from '@devacfr/bootstrap';
import { EucegService, SubmitterList, SubmitterService, SubmitterStatus } from '@devacfr/euceg';
import { I18nService, NotifierService, TableOptions } from '@devacfr/layout';
import { ErrorResponse, Pageable, PageObserver } from '@devacfr/util';
import { FileItem, FileUploader } from 'ng2-file-upload';
import { EMPTY, Observable, Subscription } from 'rxjs';
import { catchError, finalize, switchMap, tap } from 'rxjs/operators';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-submitters',
    templateUrl: './submitters.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SubmittersComponent implements OnDestroy {
    public tableOptions: TableOptions = {
        columns: [
            {
                name: 'submitterId',
                sort: true,
                i18n: 'submitters.list.fields.submitterId',
            },
            {
                name: 'name',
                sort: true,
                i18n: 'submitters.list.fields.name',
            },
            {
                name: 'address',
                sort: false,
                i18n: 'submitters.list.fields.address',
            },
            {
                name: 'country',
                sort: true,
                i18n: 'submitters.list.fields.country',
            },
            {
                name: 'phone',
                sort: false,
                i18n: 'submitters.list.fields.phone',
            },
            {
                name: 'email',
                sort: false,
                class: 'd-none d-xl-table-cell',
                i18n: 'submitters.list.fields.email',
            },
            {
                name: 'status',
                sort: true,
                i18n: 'submitters.list.fields.status',
            },
            {
                name: 'action',
                sort: false,
                i18n: 'submitters.list.fields.action',
                align: 'center',
            },
        ],
    };

    public readonly: boolean;

    public uploader: FileUploader;
    public errorResponse: ErrorResponse | undefined;

    @ViewChild('inputFile', { static: true })
    public _inputFile!: ElementRef;

    public page: PageObserver<SubmitterList>;
    public currentPageable: Pageable;

    private _subscriptions = new Subscription();
    private _block = new BlockUI('#m_portlet_submitters');
    private _blockPage = new BlockUI();
    constructor(
        public svgIcons: SvgIcons,
        public euceg: EucegService,
        private _route: ActivatedRoute,
        private _router: Router,
        private _submitterService: SubmitterService,
        private _notifierService: NotifierService,
        private _i8n: I18nService,
        private _authProvider: AuthProvider,
        private _cd: ChangeDetectorRef
    ) {
        this.uploader = this.createImportFileUploader();
        this.readonly = this._route.snapshot.data.readOnly;
        this.currentPageable = Pageable.of(0, 20).order().set('name').end();
        this.page = (obs: Observable<Pageable>) => {
            return obs.pipe(
                tap(() => {
                    this._block.block();
                }),
                switchMap((pageable) => {
                    return this._submitterService.page(pageable).pipe(finalize(() => this._block.release()));
                }),
                catchError((err) => {
                    this._notifierService.error(err);
                    return EMPTY;
                })
            );
        };
    }

    public ngOnDestroy(): void {
        this._subscriptions.unsubscribe();
    }

    public refresh(): void {
        this.currentPageable = this.currentPageable.first();
        this._cd.detectChanges();
    }

    public deleteSubmitter(submitter: SubmitterList) {
        Swal.fire({
            title: this._i8n.instant('submitters.alert.delete.title'),
            html: this._i8n.instant('submitters.alert.delete.msg', { submitterId: submitter.submitterId }).toString(),
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: this._i8n.instant('global.button.delete'),
            cancelButtonText: this._i8n.instant('global.button.cancel'),
        }).then((result) => {
            if (result.isConfirmed) {
                this._submitterService.delete(submitter.submitterId).subscribe({
                    next: () => {
                        this._notifierService.success('Submitter has been deleted.');
                        this.refresh();
                    },
                    error: (err) => this._notifierService.error(err),
                });
            }
        });
    }

    public clearFilter() {
        this.refresh();
        this.currentPageable.clearFilter();
        this.currentPageable.search = undefined;
    }

    public search(searchTerm: string) {
        this.refresh();
        this.currentPageable.clearFilter();
        if (searchTerm.length > 0) {
            this.currentPageable.search = searchTerm;
        }
    }

    public onImport(): void {
        this._notifierService.success('Submitter has been imported.');
        this.refresh();
    }

    public createSubmitter(): void {
        this._router.navigate(['create'], { relativeTo: this._route });
    }

    public getClassStatus(status: SubmitterStatus) {
        let css = '';
        switch (status) {
            case 'DRAFT':
                css = 'badge-light-info';
                break;
            case 'IMPORTED':
                css = 'badge-light-warning';
                break;
            case 'SENT':
                css = 'badge-light-primary';
                break;
            case 'VALID':
                css = 'badge-light-success';
                break;

            default:
                css = 'badge-light-dark';
                break;
        }
        return css;
    }

    private createImportFileUploader(): FileUploader {
        const uploader = new FileUploader({
            allowedMimeType: [
                'application/vnd.ms-excel',
                'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
            ],
            removeAfterUpload: true,
            autoUpload: true,
            itemAlias: 'file',
            authToken: 'Bearer ' + this._authProvider.getLocalPrincipal()?.token,
            url: BACKEND_SERVER_API_URL + 'submitters/import',
        });
        uploader.onBeforeUploadItem = () => {
            this._blockPage.block();
        };
        uploader.onCompleteItem = (item: FileItem, response: string, status: number): any => {
            if (status < 200 || status > 210) {
                this.errorResponse = JSON.parse(response);
            }
        };
        uploader.onCompleteAll = () => {
            this._blockPage.release();
            if (this.errorResponse) {
                this._notifierService.error(this.errorResponse);
            } else {
                this.onImport();
            }
        };

        return uploader;
    }

    public openFileDialog(): void {
        const input = this._inputFile?.nativeElement as HTMLInputElement;
        if (input) {
            input.value = '';
            this.errorResponse = undefined;
            this.uploader.clearQueue();
            input.click();
        }
    }
}
