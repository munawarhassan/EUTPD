import { ChangeDetectionStrategy, Component } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { SvgIcons } from '@devacfr/bootstrap';
import { UpdatePassword, User, UserAdminService } from '@devacfr/core';
import { EqualToValidator } from '@devacfr/forms';
import { NotifierService } from '@devacfr/layout';
import _ from 'lodash-es';
import { BsModalRef } from 'ngx-bootstrap/modal';

@Component({
    selector: 'app-change-password-modal',
    templateUrl: 'change-password-modal.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ChangePasswordModalComponent {
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
            password: [null, [Validators.required, Validators.minLength(5), Validators.maxLength(50)]],
            passwordConfirm: [null, [Validators.required, EqualToValidator.equalTo('password')]],
        });
    }

    public get password(): FormControl {
        return this.formControl.get('password') as FormControl;
    }

    public get passwordConfirm(): FormControl {
        return this.formControl.get('passwordConfirm') as FormControl;
    }

    public updatePassword() {
        if (this.formControl.invalid) {
            return;
        }
        if (!this.user) return;
        const changePassword: UpdatePassword = {
            name: this.user.username,
            ...this.formControl.value,
        };
        this._userAdminService.updateUserPassword(changePassword).subscribe({
            next: () => {
                this.bsModalRef.hide();
                this.onClose();
                this._notifierService.success(`The password of user ${this.user?.displayName} has been changed.`);
            },
            error: (err) => {
                this.bsModalRef.hide();
                this._notifierService.error(err);
            },
        });
    }
}
