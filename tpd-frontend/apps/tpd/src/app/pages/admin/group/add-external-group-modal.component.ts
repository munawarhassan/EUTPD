import { Component, OnInit } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { UserAdminService } from '@devacfr/core';
import { WhiteListResult } from '@devacfr/forms';
import { NotifierService } from '@devacfr/layout';
import { Pageable } from '@devacfr/util';
import _ from 'lodash-es';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { EMPTY, Observable } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';

type UserDirectory = {
    name: string;
    description: string;
    passwordRequired: boolean;
    internal: boolean;
};

const UserDirectories: UserDirectory[] = [
    {
        name: 'Internal',
        description: 'User Internal Directory',
        passwordRequired: true,
        internal: true,
    },
    {
        name: 'InternalLdap',
        description: 'Internal with LDAP Authentification',
        passwordRequired: false,
        internal: false,
    },
    {
        name: 'Ldap',
        description: 'LDAP Directory',
        passwordRequired: false,
        internal: false,
    },
    {
        name: 'InternalActiveDirectory',
        description: 'Internal with Active Directory Authentification',
        passwordRequired: false,
        internal: false,
    },
    {
        name: 'ActiveDirectory',
        description: 'Active Directory Directory',
        passwordRequired: false,
        internal: false,
    },
];

@Component({
    selector: 'app-add-external-group-modal',
    templateUrl: './add-external-group-modal.component.html',
})
export class AddExternalGroupModalComponent implements OnInit {
    public onClose: () => void = _.noop;

    private _directories: Record<string, UserDirectory>;

    public selectedGroups: Tagify.TagData[] | undefined;
    public externalDirectory!: string;

    constructor(
        public svgIcons: SvgIcons,
        private _userAdminService: UserAdminService,
        private _notifierService: NotifierService,
        public bsModalRef: BsModalRef
    ) {
        this._directories = UserDirectories.reduce<Record<string, UserDirectory>>((amap, obj) => {
            amap[obj.name] = obj;
            return amap;
        }, {});
    }

    public ngOnInit(): void {
        this._userAdminService.getDirectories().subscribe({
            next: (dirs) => {
                const directory = dirs.find((directory) => {
                    return !this._directories[directory.name].internal;
                    // eslint-disable-next-line @typescript-eslint/no-explicit-any
                }) as any;
                this.externalDirectory = directory.name;
            },
            error: (err) => this._notifierService.error(err),
        });
    }

    public findGroups(obs: Observable<string>): Observable<WhiteListResult> {
        return obs.pipe(
            switchMap((term) => {
                const request = Pageable.of(0, 200);
                if (!term || term.length === 0) {
                    return EMPTY;
                }
                return this._userAdminService.findGroupsForDirectory(request, this.externalDirectory, `*${term}*`).pipe(
                    map((page) => ({
                        searchTerm: term,
                        data: page.content.map((group) => ({ name: group, value: group })),
                    }))
                );
            })
        );
    }

    public addGroups() {
        if (!this.externalDirectory || !this.selectedGroups || this.selectedGroups.length === 0) {
            return;
        }

        const groupNames = this.selectedGroups.map((v) => v.value);

        this._userAdminService.addGroups(this.externalDirectory, ...groupNames).subscribe({
            next: () => {
                this.bsModalRef.hide();
                this.onClose();
            },
            error: (err) => this._notifierService.error(err),
        });
    }
}
