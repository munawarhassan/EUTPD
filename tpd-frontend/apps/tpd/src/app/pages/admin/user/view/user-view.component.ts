import { Location } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnDestroy, ViewEncapsulation } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { BlockUI, BreadcrumbService, SvgIcons } from '@devacfr/bootstrap';
import { Group, User, UserAdminService } from '@devacfr/core';
import { I18nService, NotifierService } from '@devacfr/layout';
import { environment } from '@tpd/environments/environment';
import { BsModalRef, BsModalService, ModalOptions } from 'ngx-bootstrap/modal';
import { BehaviorSubject, EMPTY, Observable, Subscription } from 'rxjs';
import { catchError, map, switchMap, switchMapTo, tap } from 'rxjs/operators';
import Swal from 'sweetalert2';
import { ChangePasswordModalComponent } from './change-password-modal.component';
import { UserEditModalComponent } from './user-edit-modal.component';
import { UserRenameModalComponent } from './user-rename-modal.component';

@Component({
    selector: 'app-admin-user-view',
    styleUrls: ['./user-view.component.scss'],
    templateUrl: './user-view.component.html',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminUserViewComponent implements OnDestroy {
    public changePasswordModalRef?: BsModalRef;

    public renameUserModalRef?: BsModalRef;

    public highestPermission$: Observable<string>;
    public userSubject$ = new BehaviorSubject<string>('');
    public user$: Observable<User>;
    public groupSubject$ = new BehaviorSubject<string>('');
    public groups$: Observable<Group[]> | undefined;

    public chatEnable = true;

    private _subscriptions = new Subscription();

    constructor(
        public svgIcons: SvgIcons,
        private _location: Location,
        private _fb: FormBuilder,
        private _route: ActivatedRoute,
        private _userAdminService: UserAdminService,
        private _notifierService: NotifierService,
        private _i8n: I18nService,
        private _breadcrumbService: BreadcrumbService,
        private _modalService: BsModalService
    ) {
        this.chatEnable = environment.features.chat.enable;
        const block = new BlockUI().block();
        this.user$ = this.userSubject$.pipe(
            switchMapTo(
                this._route.paramMap.pipe(
                    switchMap((params: ParamMap) => {
                        return this._userAdminService.getUserDetails(params.get('username') as string);
                    }),
                    tap((user) => this._breadcrumbService.set('@user', user.displayName)),
                    tap(() => block.release()),
                    catchError((err) => {
                        this._notifierService.error(err);
                        return EMPTY;
                    })
                )
            )
        );
        this.highestPermission$ = this._route.paramMap.pipe(
            switchMap((params: ParamMap) => {
                return this._userAdminService
                    .getHighestGlobalPermission(params.get('username') as string)
                    .pipe(map((permission) => permission.name));
            })
        );
        this.groups$ = this.groupSubject$.pipe(
            switchMapTo(
                this._route.paramMap.pipe(
                    switchMap((params: ParamMap) => {
                        return this._userAdminService
                            .findGroupsForUser(params.get('username') as string)
                            .pipe(map((request) => request.content));
                    })
                )
            )
        );
    }

    public ngOnDestroy(): void {
        this._subscriptions.unsubscribe();
    }

    public goBack() {
        this._location.back();
    }

    public deleteUser(user: User) {
        Swal.fire({
            title: this._i8n.instant('users.view.alert.delete.title'),
            html: this._i8n.instant('users.view.alert.delete.msg', { user: user.displayName }).toString(),
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: this._i8n.instant('global.button.delete'),
            cancelButtonText: this._i8n.instant('global.button.cancel'),
        }).then((result) => {
            if (result.isConfirmed) {
                this._userAdminService.deleteUser(user.username).subscribe({
                    next: () => {
                        this.goBack();
                        this._notifierService.successWithKey('users.view.notify.deleted', { user: user.displayName });
                    },
                    error: (err) => this._notifierService.error(err),
                });
            }
        });
    }

    public activate(user: User) {
        this._userAdminService.userActivation(user.username, true).subscribe({
            next: () => this.userSubject$.next('activate'),
            error: (err) => this._notifierService.error(err),
        });
    }

    public deactivate(user: User) {
        Swal.fire({
            title: this._i8n.instant('users.view.alert.deactivate.title').toString(),
            html: this._i8n.instant('users.view.alert.deactivate.msg', { user: user.displayName }).toString(),
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: this._i8n.instant('global.button.deactivate'),
            cancelButtonText: this._i8n.instant('global.button.cancel'),
        }).then((result) => {
            if (result.isConfirmed) {
                this._userAdminService.userActivation(user.username, false).subscribe({
                    next: () => this.userSubject$.next('deactivate'),
                    error: (err) => this._notifierService.error(err),
                });
            }
        });
    }

    public removeUserFromGroup(event: Event, username: string, groupname: string) {
        if (event) {
            event.stopPropagation();
            event.preventDefault();
        }
        this._userAdminService.removeUserFromGroup(username, groupname).subscribe({
            next: () => {
                this._notifierService.successWithKey('users.view.notify.removefromgroup', {
                    username,
                    group: groupname,
                });
                this.groupSubject$.next(groupname);
            },
            error: (err) => this._notifierService.error(err),
        });
    }

    public openDialogEditUser(user: User) {
        const initialState: ModalOptions = {
            initialState: {
                user: user,
                onClose: () => this.userSubject$.next('edit'),
            },
            providers: [
                {
                    provide: FormBuilder,
                    useValue: this._fb,
                },
            ],
            animated: true,
            backdrop: true,
            class: 'modal-dialog-centered',
        };
        this.changePasswordModalRef = this._modalService.show(UserEditModalComponent, initialState);
    }

    public openDialogChangePassword(user: User) {
        const initialState: ModalOptions = {
            initialState: {
                user: user,
                onClose: () => this.userSubject$.next('change-password'),
            },
            providers: [
                {
                    provide: FormBuilder,
                    useValue: this._fb,
                },
            ],
            animated: true,
            backdrop: true,
            class: 'modal-dialog-centered',
        };
        this.changePasswordModalRef = this._modalService.show(ChangePasswordModalComponent, initialState);
    }

    public openDialogRenameUser(user: User) {
        const initialState: ModalOptions = {
            initialState: {
                user: user,
                onClose: () => this.userSubject$.next('rename'),
            },
            providers: [
                {
                    provide: FormBuilder,
                    useValue: this._fb,
                },
            ],
            animated: true,
            backdrop: true,
            class: 'modal-sm modal-dialog-centered',
        };
        this.renameUserModalRef = this._modalService.show(UserRenameModalComponent, initialState);
    }
}
