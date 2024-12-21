import { Location } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { BlockUI, BreadcrumbService, SvgIcons } from '@devacfr/bootstrap';
import { Group, User, UserAdminService } from '@devacfr/core';
import { I18nService, NotifierService } from '@devacfr/layout';
import { BsModalRef, BsModalService, ModalOptions } from 'ngx-bootstrap/modal';
import { BehaviorSubject, combineLatest, EMPTY, Observable, Subject } from 'rxjs';
import { catchError, finalize, map, switchMap, tap } from 'rxjs/operators';
import Swal from 'sweetalert2';
import { AddUserModalComponent } from './add-user-modal.component';

@Component({
    selector: 'app-group-view',
    templateUrl: './group-view.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminGroupViewComponent {
    public group$: Observable<Group>;
    public users$: Observable<User[]> | undefined;

    public userFilter = '';

    private block = new BlockUI('#m_portlet_group_view');
    private _subjectAction = new BehaviorSubject<string | undefined>(undefined);

    constructor(
        public svgIcons: SvgIcons,
        private _fb: FormBuilder,
        private _cd: ChangeDetectorRef,
        private _route: ActivatedRoute,
        private _location: Location,
        private _userAdminService: UserAdminService,
        private _notifierService: NotifierService,
        private _i8n: I18nService,
        private _breadcrumbService: BreadcrumbService,
        private _modalService: BsModalService
    ) {
        this.group$ = combineLatest([this._route.paramMap, this._subjectAction]).pipe(
            map(([params]) => params),
            tap(() => this.block.block()),
            switchMap((params: ParamMap) => {
                const groupName = params.get('name');
                if (groupName)
                    return this._userAdminService.getGroupDetails(groupName).pipe(
                        tap((grp) => this._breadcrumbService.set('@group', grp.name)),
                        finalize(() => {
                            this._cd.detectChanges();
                            this.block.release();
                        })
                    );
                else return EMPTY;
            }),
            catchError((err) => {
                this._notifierService.error(err);
                return EMPTY;
            })
        );

        this.users$ = combineLatest([this._route.paramMap, this._subjectAction]).pipe(
            map(([params]) => params),
            switchMap((params: ParamMap) => {
                const groupName = params.get('name');
                if (groupName)
                    return this._userAdminService.findUsersInGroup(groupName).pipe(
                        map((req) => req.content),
                        finalize(() => {
                            this._cd.detectChanges();
                        })
                    );
                else return EMPTY;
            }),
            catchError((err) => {
                this._notifierService.error(err);
                return EMPTY;
            })
        );
    }

    public goBack(): void {
        this._location.back();
    }

    public deleteGroup(group: Group): void {
        const groupeName = group.name;
        Swal.fire({
            title: this._i8n.instant('groups.view.alert.delete.title'),
            html: this._i8n.instant('groups.view.alert.delete.msg', { name: groupeName }).toString(),
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: this._i8n.instant('global.button.delete'),
            cancelButtonText: this._i8n.instant('global.button.cancel'),
        }).then((result) => {
            if (result.isConfirmed) {
                this._userAdminService.deleteGroup(groupeName).subscribe({
                    next: () => {
                        this._notifierService.successWithKey('groups.view.notify.deleted', { name: groupeName });
                        this.goBack();
                    },
                    error: (err) => this._notifierService.error(err),
                });
            }
        });
    }

    public removeUserFromGroup(group: Group, user: User) {
        const groupeName = group.name;
        this._userAdminService.removeUserFromGroup(user.username, groupeName).subscribe({
            next: () => {
                this._notifierService.successWithKey('groups.view.notify.removeUserFromGroup', {
                    user: user.displayName,
                    name: groupeName,
                });
                this._subjectAction.next('refresh');
            },
            error: (err) => this._notifierService.error(err),
        });
    }

    public openDialogAddUser(group: Group) {
        const initialState: ModalOptions = {
            initialState: {
                group: group,
                onClose: () => this._subjectAction.next('refresh'),
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
        this._modalService.show(AddUserModalComponent, initialState);
    }
}
