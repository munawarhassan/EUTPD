import { Component, EventEmitter, Output } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { SvgIcons } from '@devacfr/bootstrap';
import { KeystoreService } from '@devacfr/core';
import { NotifierService } from '@devacfr/layout';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import Swal from 'sweetalert2';
import { ChangeAliasModalComponent } from './change-alias-modal.component';

@Component({
    selector: 'app-import-keypair-modal',
    templateUrl: './import-keypair-modal.component.html',
})
export class ImportKeypairModalComponent {
    @Output()
    public closeModal = new EventEmitter<void>(false);

    @Output()
    public cancel = new EventEmitter<void>(false);

    public alertMessage: string | undefined;

    public theForm: FormGroup;

    constructor(
        public svgIcons: SvgIcons,
        private _fb: FormBuilder,
        public bsModalRef: BsModalRef,
        private _keystoreService: KeystoreService,
        private _notifierService: NotifierService,
        private _modalService: BsModalService
    ) {
        this.theForm = this._fb.group({
            password: [null, [Validators.required]],
            file: [null, [Validators.required]],
        });
    }

    get password(): FormControl {
        return this.theForm.get('password') as FormControl;
    }

    get file(): FormControl {
        return this.theForm.get('file') as FormControl;
    }

    public onSubmit() {
        if (this.theForm.invalid) return;
        const files = this.file.value as FileList;
        this.validateKeyPair(files[0], this.password.value);
    }

    public onClose() {
        this.closeModal.emit();
        this.bsModalRef.hide();
    }

    public onCancel(): void {
        this.cancel.emit();
        this.bsModalRef.hide();
    }

    private validateKeyPair(file: File, password: string) {
        this.alertMessage = undefined;
        this._keystoreService.validateKeyPair(file, password).subscribe({
            next: (resp) => {
                const alias = resp.alias.toLocaleLowerCase();
                this.openChangeAliasName(alias, (aliasName) => {
                    this.addKeyPair(file, aliasName, password);
                });
            },
            error: (err) => {
                this.alertMessage = err.error.message;
            },
        });
    }

    private addKeyPair(file: File, alias: string, password: string) {
        this.checkEntryExist('Key pair', alias, () => {
            this._keystoreService.importKeyPair(file, alias, password).subscribe({
                next: () => {
                    this._notifierService.success('The Key Pair ' + alias + ' has been imported.');
                    this.onClose();
                },
                error: (err) => {
                    this._notifierService.error(err);
                },
            });
        });
    }

    private checkEntryExist(type: string, alias: string, callback: () => void) {
        this._keystoreService.exists(alias).subscribe({
            next: (status) => {
                if (status) {
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
            error: () => callback(),
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
            ignoreBackdropClick: true,
            initialState: context,
        });
        const modal = modalRef.content;
        modal?.closeModal.subscribe((aliasName) => {
            callback(aliasName);
        });
    }
}
