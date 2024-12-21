import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormControl, Validators } from '@angular/forms';
import { BsModalRef } from 'ngx-bootstrap/modal';

@Component({
    selector: 'app-change-alias-modal',
    templateUrl: './change-alias-modal.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ChangeAliasModalComponent implements OnInit {
    @Input()
    public alias: string | undefined;

    @Output()
    public closeModal = new EventEmitter<string>();

    @Output()
    public cancel = new EventEmitter<void>();

    public theForm = this._fb.group({
        alias: ['', Validators.required],
    });

    constructor(private _fb: FormBuilder, public bsModalRef: BsModalRef) {}

    ngOnInit() {
        this.theForm.patchValue({
            alias: this.alias,
        });
    }

    public get aliasCtrl(): FormControl {
        return this.theForm.get('alias') as FormControl;
    }

    public onClose() {
        if (this.theForm.invalid) return;
        this.closeModal.emit(this.aliasCtrl.value);
        this.bsModalRef.hide();
    }

    public onCancel(): void {
        this.cancel.emit();
        this.bsModalRef.hide();
    }
}
