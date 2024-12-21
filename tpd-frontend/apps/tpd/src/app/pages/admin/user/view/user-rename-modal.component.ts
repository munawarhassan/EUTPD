import { ChangeDetectionStrategy, Component } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { SvgIcons } from '@devacfr/bootstrap';
import { RenameUser, User, UserAdminService } from '@devacfr/core';
import { NotifierService } from '@devacfr/layout';
import _ from 'lodash-es';
import { BsModalRef } from 'ngx-bootstrap/modal';

@Component({
    selector: 'app-user-rename-modal',
    templateUrl: './user-rename-modal.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserRenameModalComponent {
    public user: User | undefined;

    public formControl: FormGroup;

    public onClose: () => void = _.noop;

    constructor(
        public svgIcons: SvgIcons,
        public bsModalRef: BsModalRef,
        private _fb: FormBuilder,
        private _userAdminService: UserAdminService,
        private _notifierService: NotifierService
    ) {
        this.formControl = this._fb.group({
            newName: [
                null,
                [
                    Validators.required,
                    Validators.minLength(3),
                    Validators.maxLength(20),
                    Validators.pattern('[a-z0-9]*'),
                ],
            ],
        });
    }

    public get newName(): FormControl {
        return this.formControl.get('newName') as FormControl;
    }

    public rename() {
        if (this.formControl.invalid) {
            return;
        }
        if (!this.user) return;
        const renameUser: RenameUser = {
            name: this.user.username,
            newName: this.newName.value,
        };
        this._userAdminService.renameUser(renameUser).subscribe({
            next: () => {
                this.bsModalRef.hide();
                this.onClose();
                this._notifierService.success('The name of user has been changed.');
            },
            error: (err) => this._notifierService.error(err),
        });
    }
}
