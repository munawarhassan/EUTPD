import { ChangeDetectionStrategy, Component } from '@angular/core';
import { BlockUI, SvgIcons } from '@devacfr/bootstrap';
import { Directory, User, UserAdminService } from '@devacfr/core';
import { I18nService, NotifierService, TableLayer, TableOptions } from '@devacfr/layout';
import { PageObserver, Pageable } from '@devacfr/util';
import { EMPTY, Observable } from 'rxjs';
import { catchError, finalize, switchMap, tap } from 'rxjs/operators';
import Swal from 'sweetalert2';
@Component({
    selector: 'app-users',
    templateUrl: './users.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminUsersComponent {
    public tableOptions: TableOptions = {
        columns: [
            {
                name: 'username',
                sort: true,
                i18n: 'users.fields.username',
            },
            {
                name: 'directory',
                sort: true,
                i18n: 'users.fields.directory',
            },
            {
                name: 'activated',
                sort: true,
                i18n: 'users.fields.active',
                align: 'center',
            },
            {
                name: 'Action',
                sort: false,
                i18n: 'users.fields.action',
                align: 'end',
            },
        ],
    };

    public layer: TableLayer = 'line';
    public page: PageObserver<User>;
    public currentPageable: Pageable;

    private _block = new BlockUI('#m_portlet_users');
    constructor(
        public svgIcons: SvgIcons,
        private _userAdminService: UserAdminService,
        private _notifierService: NotifierService,
        private _i8n: I18nService
    ) {
        this.currentPageable = Pageable.of(0, 20).order().set('username').end();
        this.page = (obs: Observable<Pageable>) => {
            return obs.pipe(
                tap(() => {
                    this._block.block();
                }),
                switchMap((pageable) => {
                    return this._userAdminService.findUsers(pageable).pipe(finalize(() => this._block.release()));
                }),
                catchError((err) => {
                    this._notifierService.error(err);
                    return EMPTY;
                })
            );
        };
    }

    public trackUser(index: number, user: User): string {
        return user.username;
    }

    public directory(name: string): Directory | undefined {
        const dir = this._userAdminService.getDirectory(name);
        if (dir) {
            return dir;
        }
        return undefined;
    }

    public refresh(): void {
        this.currentPageable = this.currentPageable.first();
    }

    public clearFilter() {
        this.refresh();
        this.currentPageable.clearFilter();
    }

    public search(searchTerm: string) {
        this.refresh();
        this.currentPageable.clearFilter();
        if (searchTerm.length > 0) {
            this.currentPageable.filter().contains('displayName', searchTerm);
        }
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
                        this.refresh();
                        this._notifierService.successWithKey('users.view.notify.deleted', { user: user.displayName });
                    },
                    error: (err) => this._notifierService.error(err),
                });
            }
        });
    }
}
