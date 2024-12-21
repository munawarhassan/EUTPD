import { Component } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { Group, User, UserAdminService } from '@devacfr/core';
import { WhiteListResult } from '@devacfr/forms';
import { NotifierService } from '@devacfr/layout';
import { Pageable } from '@devacfr/util';
import Tagify from '@yaireo/tagify';
import _ from 'lodash-es';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { EMPTY, Observable } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';

@Component({
    selector: 'app-add-user-modal',
    templateUrl: 'add-user-modal.component.html',
})
export class AddUserModalComponent {
    public group!: Group;

    public onClose: () => void = _.noop;

    public selectedUsers: Tagify.TagData[] | undefined;

    constructor(
        public svgIcons: SvgIcons,
        public bsModalRef: BsModalRef,
        private _userAdminService: UserAdminService,
        private _notifierService: NotifierService
    ) {}

    public addUsers(): void {
        if (!this.selectedUsers || this.selectedUsers.length === 0) {
            return;
        }
        this.addUsersToGroup(this.group, this.selectedUsers as unknown[] as User[]);
    }

    public findUsersNotInGroup(obs: Observable<string>): Observable<WhiteListResult> {
        return obs.pipe(
            switchMap((term) => {
                if (!term || term.length === 0) {
                    return EMPTY;
                }
                const request = Pageable.of(0, 200);
                const groupeName = this.group.name;
                if (term) {
                    request.filter().contains('displayName', term);
                }
                return this._userAdminService.findUsersNotInGroup(groupeName, request).pipe(
                    map((resp) => ({
                        searchTerm: term,
                        data: resp.content.map((user) => ({ ...user, value: user.username })),
                    }))
                );
            })
        );
    }

    public addUsersToGroup(group: Group, groupAndUsers: User[]) {
        const groupeName = group.name;
        this._userAdminService.addUsersToGroup(groupeName, ...groupAndUsers.map((user) => user.username)).subscribe({
            next: () => {
                this._notifierService.successWithKey('groups.view.notify.addUsersToGroup', {
                    users: groupAndUsers.map((user) => user.displayName).join(),
                    name: groupeName,
                });
                this.bsModalRef.hide();
                this.onClose();
            },
            error: (err) => this._notifierService.error(err),
        });
    }
}
