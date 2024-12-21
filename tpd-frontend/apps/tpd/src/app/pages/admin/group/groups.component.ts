import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { BlockUI, SvgIcons } from '@devacfr/bootstrap';
import { Group, User, UserAdminService } from '@devacfr/core';
import { I18nService, NotifierService, TableOptions } from '@devacfr/layout';
import { PageObserver, Pageable } from '@devacfr/util';
import { BsModalService, ModalOptions } from 'ngx-bootstrap/modal';
import { EMPTY, Observable } from 'rxjs';
import { catchError, finalize, map, switchMap, tap } from 'rxjs/operators';
import Swal from 'sweetalert2';
import { AddExternalGroupModalComponent } from './add-external-group-modal.component';
import { CreateGroupModalComponent } from './create-group-modal.component';

@Component({
    selector: 'app-admin-groups',
    templateUrl: './groups.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminGroupsComponent {
    public groups: Group[] = [];

    public tableOptions: TableOptions = {
        columns: [
            {
                name: 'name',
                sort: true,
                i18n: 'groups.fields.name',
            },
            {
                name: 'actions',
                title: 'Actions',
                sort: false,
                align: 'end',
            },
        ],
    };

    public page: PageObserver<Group>;

    public set currentPageable(value: Pageable) {
        this._currentPageable = value;
        this._cd.detectChanges();
    }

    public get currentPageable(): Pageable {
        return this._currentPageable;
    }

    private _block = new BlockUI('#m_portlet_groups');
    public _currentPageable: Pageable;

    constructor(
        public svgIcons: SvgIcons,
        private _fb: FormBuilder,
        private _cd: ChangeDetectorRef,
        private _userAdminService: UserAdminService,
        private _notifierService: NotifierService,
        private _i8n: I18nService,
        private _modalService: BsModalService
    ) {
        this._currentPageable = Pageable.of(0, 20).order().set('name').end();

        this.page = (obs: Observable<Pageable>) => {
            return obs.pipe(
                tap(() => {
                    this._block.block();
                }),
                switchMap((pageable) => {
                    return this._userAdminService.findGroups(pageable).pipe(finalize(() => this._block.release()));
                }),
                catchError((err) => {
                    this._notifierService.error(err);
                    return EMPTY;
                })
            );
        };
    }

    public trackGroup(index: number, group: Group): string {
        return group.name;
    }

    public search(searchTerm: string) {
        this.currentPageable = this.currentPageable.first();
        this.currentPageable.clearFilter();
        if (searchTerm.length > 0) {
            this.currentPageable.filter().contains('name', searchTerm);
        }
    }

    public clearFilter() {
        this.currentPageable = this.currentPageable.first();
        this.currentPageable.clearFilter();
    }

    public findUsersInGroup(group: Group): (obs) => Observable<User[]> {
        return (obs: Observable<unknown>) =>
            obs.pipe(
                switchMap(() => this._userAdminService.findUsersInGroup(group.name).pipe(map((page) => page.content)))
            );
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
        }).then((isConfirm) => {
            if (isConfirm.value) {
                this._userAdminService.deleteGroup(groupeName).subscribe({
                    next: () => {
                        this.currentPageable = this.currentPageable.first();
                        this._notifierService.successWithKey('groups.view.notify.deleted', { name: groupeName });
                    },
                    error: (err) => this._notifierService.error(err),
                });
            }
        });
    }

    public openDialogCreateGroupModal() {
        const initialState: ModalOptions = {
            initialState: {
                onClose: () => (this.currentPageable = this.currentPageable.first()),
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
        this._modalService.show(CreateGroupModalComponent, initialState);
    }

    public openDialogAddExternalGroupModal() {
        const initialState: ModalOptions = {
            initialState: {
                onClose: () => (this.currentPageable = this.currentPageable.first()),
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
        this._modalService.show(AddExternalGroupModalComponent, initialState);
    }
}
