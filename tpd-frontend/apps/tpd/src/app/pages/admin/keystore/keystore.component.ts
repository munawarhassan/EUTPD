import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { BlockUI, SvgIcons } from '@devacfr/bootstrap';
import { KeystoreRequest, KeystoreService } from '@devacfr/core';
import { I18nService, NotifierService, TableOptions } from '@devacfr/layout';
import { Page, Pageable } from '@devacfr/util';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BehaviorSubject, EMPTY, Observable, Subject, Subscription } from 'rxjs';
import { catchError, finalize, switchMap, tap } from 'rxjs/operators';
import Swal from 'sweetalert2';
import { ChangeAliasModalComponent } from './change-alias-modal.component';
import { ImportKeypairModalComponent } from './import-keypair-modal.component';
import { KeystoreNotificationModalComponent } from './keystore-notification-modal.component';

@Component({
    selector: 'app-keystore',
    templateUrl: './keystore.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KeystoreComponent implements OnDestroy {
    public currentPage$: Subject<Pageable>;
    public page$: Observable<Page<KeystoreRequest>>;

    public tableOptions: TableOptions = {
        columns: [
            {
                name: 'type',
                sort: true,
                i18n: 'keystore.fields.type',
            },
            {
                name: 'alias',
                sort: true,
                i18n: 'keystore.fields.alias',
            },
            {
                name: 'keySize',
                sort: true,
                i18n: 'keystore.fields.keySize',
            },
            {
                name: 'algorithm',
                sort: true,
                i18n: 'keystore.fields.algorithm',
            },
            {
                name: 'expiredDate',
                sort: true,
                i18n: 'keystore.fields.expiredDate',
            },
            {
                name: 'lastModified',
                sort: true,
                i18n: 'keystore.fields.lastModified',
            },
            {
                name: 'action',
                sort: false,
                i18n: 'keystore.fields.action',
                align: 'center',
            },
        ],
    };

    private _subscriptions = new Subscription();

    private _currentPageable: Pageable;
    private _block = new BlockUI('#m_portlet_keystore');

    constructor(
        public svgIcons: SvgIcons,
        private _fb: FormBuilder,
        private _keystoreService: KeystoreService,
        private _notifierService: NotifierService,
        private _i8n: I18nService,
        private _modalService: BsModalService
    ) {
        this._currentPageable = Pageable.of(0, 20).order().set('alias').end();

        this.currentPage$ = new BehaviorSubject<Pageable>(this._currentPageable);
        this.page$ = this.currentPage$.pipe(
            tap((pageable) => {
                this._currentPageable = pageable;
                this._block.block();
            }),

            switchMap((pageable) =>
                this._keystoreService.findAll(pageable).pipe(finalize(() => this._block.release()))
            ),
            catchError((err) => {
                this._notifierService.error(err);
                return EMPTY;
            })
        );
    }

    ngOnDestroy(): void {
        this._subscriptions.unsubscribe();
    }

    public get currentPageable(): Pageable {
        return this._currentPageable;
    }

    public refresh(): void {
        this.currentPage$.next(this.currentPageable);
    }

    public clearFilter() {
        this.currentPageable.clearFilter();
        this.currentPage$.next(this.currentPageable);
    }

    public search(searchTerm: string) {
        this.currentPageable.clearFilter();
        if (searchTerm.length > 0) {
            this.currentPageable.filter().contains('alias', searchTerm);
        }
        this.currentPage$.next(this.currentPageable);
    }

    public deleteKey(alias: KeystoreRequest): void {
        Swal.fire({
            title: 'Are you sure?',
            html: 'Are you sure that you want to delete ' + alias + '?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: this._i8n.instant('global.button.delete'),
            cancelButtonText: this._i8n.instant('global.button.cancel'),
        }).then((result) => {
            if (result.isConfirmed) {
                this._keystoreService.delete(alias.alias).subscribe({
                    next: () => {
                        this._notifierService.success('The key ' + alias + ' has been removed.');
                        this.refresh();
                    },
                    error: (err) => this._notifierService.error(err),
                });
            }
        });
    }

    public onImportCert(event: Event, input: HTMLInputElement): void {
        event.preventDefault();
        input.value = '';
        input.click();
    }

    public onSelectCertificate(event: Event): void {
        const input = event.target as HTMLInputElement;
        const files = input.files;
        if (files) {
            this.importCertificate(files);
        }
    }

    public importCertificate(files: FileList) {
        if (files.length !== 1) return;
        const file = files.item(0);
        if (!file) {
            return;
        }
        this._keystoreService.validateCertificate(file).subscribe({
            next: (alias) => {
                alias = alias.toLocaleLowerCase();
                this.openChangeAliasName(alias, (aliasName) => {
                    this.addCertificate(file, aliasName);
                });
            },
            error: (err) => {
                this._notifierService.error(err);
            },
        });
    }

    private checkEntryExist(type: string, alias: string, callback: () => void) {
        this._keystoreService.exists(alias).subscribe({
            next: (exist) => {
                if (exist) {
                    Swal.fire({
                        title: 'Are you sure?',
                        html: 'You will replace the ' + type + ' ' + alias + '!',
                        icon: 'warning',
                        showCancelButton: true,
                        confirmButtonText: 'Yes, replace it!',
                        cancelButtonText: 'No, cancel!',
                    }).then((result) => {
                        if (result.isConfirmed) {
                            callback();
                        }
                    });
                } else {
                    callback();
                }
            },
            error: (err) => {
                this._notifierService.error(err);
            },
        });
    }

    public importKeypair() {
        const modalRef = this._modalService.show(ImportKeypairModalComponent, {
            providers: [
                {
                    provide: FormBuilder,
                    useValue: this._fb,
                },
                {
                    provide: KeystoreService,
                    useValue: this._keystoreService,
                },
                {
                    provide: NotifierService,
                    useValue: this._notifierService,
                },
                {
                    provide: BsModalService,
                    useValue: this._modalService,
                },
            ],
            animated: true,
            backdrop: true,
            class: 'modal-lg modal-dialog-centered',
        });
        modalRef.onHidden?.subscribe(() => this.refresh());
    }

    private addCertificate(file: File, alias: string) {
        this.checkEntryExist('Certificate', alias, () => {
            this._keystoreService.importCertificate(file, alias).subscribe({
                next: () => {
                    this._notifierService.success('The Trust Certificate ' + alias + ' has been imported.');
                    this.refresh();
                },
                error: (err) => {
                    this._notifierService.error(err);
                },
            });
        });
    }

    public notification() {
        this._modalService.show(KeystoreNotificationModalComponent, {
            providers: [
                {
                    provide: FormBuilder,
                    useValue: this._fb,
                },
            ],
            animated: true,
            backdrop: true,
            class: 'modal-lg modal-dialog-centered',
        });
    }

    public openChangeAliasName(alias: string, callback: (alias: string) => void) {
        const context = {
            alias,
        };
        const modalRef = this._modalService.show(ChangeAliasModalComponent, {
            providers: [
                {
                    provide: FormBuilder,
                    useValue: this._fb,
                },
            ],
            animated: true,
            backdrop: true,
            initialState: context,
            class: 'modal-lg modal-dialog-centered',
        });
        const modal = modalRef.content;
        modal?.closeModal.subscribe((aliasName) => {
            callback(aliasName);
        });
    }
}
