import { ChangeDetectionStrategy, Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { BlockUI } from '@devacfr/bootstrap';
import { KeystoreService } from '@devacfr/core';
import { NotifierService } from '@devacfr/layout';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subscription } from 'rxjs';
import { finalize } from 'rxjs/operators';

@Component({
    selector: 'app-keystore-notification-modal',
    templateUrl: './keystore-notification-modal.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KeystoreNotificationModalComponent implements OnInit, OnDestroy {
    @Output()
    public closeModal = new EventEmitter<void>(false);

    @Output()
    public cancel = new EventEmitter<void>(false);

    public theForm: FormGroup;

    private _block = new BlockUI('#m_notification_modal');

    private _subscription = new Subscription();
    constructor(
        private _fb: FormBuilder,
        public bsModalRef: BsModalRef,
        private _keyStoreService: KeystoreService,
        private _notifierService: NotifierService
    ) {
        this.theForm = this._fb.group({
            enable: [''],
            contact: ['', [Validators.required, Validators.email]],
        });
        this.enable.valueChanges.subscribe((enable) => {
            if (enable) this.contact.enable();
            else this.contact.disable();
        });
    }

    ngOnInit() {
        this._block.block();
        this._subscription.add(
            this._keyStoreService
                .getSettings()
                .pipe(finalize(() => this._block.release()))
                .subscribe({
                    next: (settings) => this.theForm.patchValue(settings),
                    error: (err) => this._notifierService.error(err),
                })
        );
    }
    ngOnDestroy(): void {
        this._subscription.unsubscribe();
    }

    public save() {
        if (this.theForm.invalid) {
            return;
        }
        this._keyStoreService.saveSettings(this.theForm.value).subscribe({
            next: () => {
                this._notifierService.success('The Key Store notification settings has been changed.');
                this.onClose();
            },
            error: (resp) => {
                this._notifierService.error(resp);
            },
        });
    }

    public get enable(): FormControl {
        return this.theForm.get('enable') as FormControl;
    }

    public get contact(): FormControl {
        return this.theForm.get('contact') as FormControl;
    }

    public onClose() {
        this.closeModal.emit();
        this.bsModalRef.hide();
    }

    public onCancel(): void {
        this.cancel.emit();
        this.bsModalRef.hide();
    }
}
