import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { BlockUI, SvgIcons } from '@devacfr/bootstrap';
import { GlobalPermissionService, PermittedGroup, PermittedUser } from '@devacfr/core';
import { WhiteListResult } from '@devacfr/forms';
import { I18nService, NotifierService } from '@devacfr/layout';
import { Pageable } from '@devacfr/util';
import Tagify from '@yaireo/tagify';
import { EMPTY, Observable, Subscription } from 'rxjs';
import { finalize, map, switchMap } from 'rxjs/operators';

@Component({
    selector: 'app-globalpermission',
    templateUrl: './globalpermission.component.html',
    styleUrls: ['./globalpermisson.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminGlobalPermissionComponent implements OnInit, OnDestroy {
    public userPageRequest: Pageable;
    public groupPageRequest: Pageable;
    public userAccess: PermittedUser[] = [];
    public groupAccess: PermittedGroup[] = [];

    public selectedUserPermission = 'USER';
    public selectedGroupPermission = 'USER';

    public groupToAdd: Tagify.TagData[] | undefined;
    public userToAdd: Tagify.TagData[] | undefined;

    private _blockUser = new BlockUI('#m_portlet_globalpermission_user');
    private _blockGroup = new BlockUI('#m_portlet_globalpermission_group');

    public permissions: { name: string; value: string }[];
    private _subscription = new Subscription();

    constructor(
        public svgIcons: SvgIcons,
        private _cd: ChangeDetectorRef,
        private _globalPermissionAdminService: GlobalPermissionService,
        private _notifierService: NotifierService,
        private _i18nService: I18nService
    ) {
        this.userPageRequest = Pageable.of(0, 200);
        // this.userPageRequest.order().set('user.username', 'ASC');
        this.groupPageRequest = Pageable.of(0, 200);
        // this.groupPageRequest.order().set('group', 'ASC');

        this.permissions = this.getTranslatedPermissions();
        this._subscription.add(
            this._i18nService.changed.subscribe(() => (this.permissions = this.getTranslatedPermissions()))
        );
    }

    public ngOnInit() {
        this.refresh();
    }

    ngOnDestroy(): void {
        this._subscription.unsubscribe();
    }

    public refresh(): void {
        this.selectedUserPermission = 'USER';
        this.selectedGroupPermission = 'USER';
        this.groupToAdd = [];
        this.userToAdd = [];

        this._blockUser.block();
        this._globalPermissionAdminService
            .getUsersWithAnyPermission(this.userPageRequest)
            .pipe(finalize(() => this._blockUser.release()))
            .subscribe({
                next: (resp) => {
                    this.userAccess = resp.content || [];
                    this.userPageRequest = resp.pageable;
                    this._cd.detectChanges();
                },
                error: (err) => this._notifierService.error(err),
            });

        this._blockGroup.block();
        this._globalPermissionAdminService
            .getGroupsWithAnyPermission(this.groupPageRequest)
            .pipe(finalize(() => this._blockGroup.release()))
            .subscribe({
                next: (resp) => {
                    this.groupAccess = resp.content || [];
                    this.groupPageRequest = resp.pageable;
                    this._cd.detectChanges();
                },
                error: (err) => this._notifierService.error(err),
            });
    }

    public onGroupPermissionChange(evt: MouseEvent, access: PermittedGroup, permission: string): void {
        const target = evt.target as HTMLInputElement;
        if (target.checked) {
            access.permission = permission;
        } else {
            switch (permission) {
                case 'SYS_ADMIN':
                    access.permission = 'ADMIN';
                    break;
                default:
                    access.permission = 'USER';
                    break;
            }
        }
        this.setGroupPermission([access.group], access.permission);
    }

    public onUserPermissionChange(evt: MouseEvent, access: PermittedUser, permission: string): void {
        const target = evt.target as HTMLInputElement;
        if (target.checked) {
            access.permission = permission;
        } else {
            switch (permission) {
                case 'SYS_ADMIN':
                    access.permission = 'ADMIN';
                    break;
                default:
                    access.permission = 'USER';
                    break;
            }
        }
        this.setUserPermission([access.user.username], access.permission);
    }

    public setUserPermission(usernames: Tagify.TagData[] | string[] | undefined, permission: string): void {
        if (!usernames) {
            return;
        }
        this._blockUser.block();
        this._globalPermissionAdminService
            .setPermissionForUsers(
                permission,
                ...usernames.map((user) => (typeof user === 'string' ? user : user.value))
            )
            .subscribe({
                next: () => {
                    this._blockUser.release();
                    this.refresh();
                },
                error: (err) => {
                    this._blockUser.release();
                    this._notifierService.error(err);
                    this.refresh();
                },
            });
    }

    public getUsersWithoutAnyPermission(obs: Observable<string>): Observable<WhiteListResult> {
        return obs.pipe(
            switchMap((term) => {
                if (!term || term.length === 0) {
                    return EMPTY;
                }
                const request = Pageable.of();
                if (term) {
                    request.filter().contains('displayName', term);
                }
                return this._globalPermissionAdminService.getUsersWithoutAnyPermission(request).pipe(
                    map((page) => ({
                        searchTerm: term,
                        data: page.content.map((user) => ({ ...user, value: user.username })),
                    }))
                );
            })
        );
    }

    public getGroupsWithoutAnyPermission(obs: Observable<string>): Observable<WhiteListResult> {
        return obs.pipe(
            switchMap((term) => {
                if (!term || term.length === 0) {
                    return EMPTY;
                }
                const request = Pageable.of();
                if (term) {
                    request.filter().contains('name', term);
                }
                return this._globalPermissionAdminService.getGroupsWithoutAnyPermission(request).pipe(
                    map((page) => ({
                        searchTerm: term,
                        data: page.content.map((group) => ({ name: group, value: group })),
                    }))
                );
            })
        );
    }

    public revokeUserPermission(username): void {
        this._blockUser.block();
        this._globalPermissionAdminService.revokePermissionsForUser(username).subscribe({
            next: () => {
                this._blockUser.release();
                this.refresh();
            },
            error: (err) => {
                this._blockUser.release();
                this._notifierService.error(err);
            },
        });
    }

    public revokeGroupPermission(groupName): void {
        this._blockGroup.block();
        this._globalPermissionAdminService.revokePermissionsForGroup(groupName).subscribe({
            next: () => {
                this._blockGroup.release();
                this.refresh();
            },
            error: (err) => {
                this._blockGroup.release();
                this._notifierService.error(err);
            },
        });
    }

    public setGroupPermission(groupNames: Tagify.TagData[] | string[] | undefined, permission: string) {
        if (!groupNames) {
            return;
        }
        this._blockGroup.block();
        this._globalPermissionAdminService
            .setPermissionForGroups(
                permission,
                ...groupNames.map((group) => (typeof group === 'string' ? group : group.value))
            )
            .subscribe({
                next: () => {
                    this._blockGroup.release();
                    this.refresh();
                },
                error: (err) => {
                    this._blockGroup.release();
                    this._notifierService.error(err);
                    this.refresh();
                },
            });
    }

    private getTranslatedPermissions(): { name: string; value: string }[] {
        return [
            {
                name: 'USER',
                value: this._i18nService.instant('global.permission.user').toString(),
            },
            {
                name: 'ADMIN',
                value: this._i18nService.instant('global.permission.admin').toString(),
            },
            {
                name: 'SYS_ADMIN',
                value: this._i18nService.instant('global.permission.sysadmin').toString(),
            },
        ];
    }
}
