import { Component } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { SvgIcons } from '@devacfr/bootstrap';
import { CreateGroup, UserAdminService } from '@devacfr/core';
import { NotifierService } from '@devacfr/layout';
import _ from 'lodash-es';
import { BsModalRef } from 'ngx-bootstrap/modal';

@Component({
    selector: 'app-create-group-modal',
    templateUrl: 'create-group-modal.component.html',
})
export class CreateGroupModalComponent {
    public formControl: FormGroup;

    public onClose: () => void = _.noop;

    constructor(
        public svgIcons: SvgIcons,
        private _fb: FormBuilder,
        private _userAdminService: UserAdminService,
        private _notifierService: NotifierService,
        public bsModalRef: BsModalRef
    ) {
        this.formControl = this._fb.group({
            groupName: [null, [Validators.required, Validators.minLength(1), Validators.maxLength(255)]],
        });
    }

    public get groupName(): FormControl {
        return this.formControl.get('groupName') as FormControl;
    }

    createGroup(): void {
        if (this.formControl.invalid) {
            return;
        }
        const createGroup: CreateGroup = {
            name: this.groupName.value,
        };
        this._userAdminService.createGroup(createGroup).subscribe({
            next: () => {
                this._notifierService.successWithKey('groups.create.notify.created', { name: createGroup.name });
                this.bsModalRef.hide();
                this.onClose();
            },
            error: (err) => this._notifierService.error(err),
        });
    }
}
