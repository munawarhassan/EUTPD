import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SvgIcons } from '@devacfr/bootstrap';
import { UpdateUserAdmin, User, UserAdminService } from '@devacfr/core';
import { NotifierService } from '@devacfr/layout';
import _ from 'lodash-es';
import { BsModalRef } from 'ngx-bootstrap/modal';

@Component({
    selector: 'app-user-edit-modal',
    templateUrl: './user-edit-modal.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserEditModalComponent implements OnInit {
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
            name: [null, [Validators.required]],
            displayName: [null, [Validators.required]],
            email: [null, [Validators.required, Validators.email, Validators.minLength(5), Validators.maxLength(100)]],
        });
    }

    ngOnInit(): void {
        if (this.user) {
            this.formControl.setValue({
                name: this.user.username,
                displayName: this.user.displayName,
                email: this.user.email,
            });
        }
    }

    public updateUser() {
        if (this.formControl.invalid) {
            return;
        }
        this._userAdminService.updateUser(this.formControl.value as UpdateUserAdmin).subscribe({
            next: () => {
                this._notifierService.successWithKey('users.view.notify.updated', {
                    user: this.user?.displayName,
                });
                this.bsModalRef.hide();
                this.onClose();
            },
            error: (err) => this._notifierService.error(err),
        });
    }
}
