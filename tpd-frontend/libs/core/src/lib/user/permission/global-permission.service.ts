import { HttpClient, HttpParams } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Page, Pageable } from '@devacfr/util';
import { Observable } from 'rxjs';
import { User } from '../typing';
import { BACKEND_SERVER_API_URL_TOKEN } from '../../shared';
import { PermittedGroup } from './permitted-group.model';
import { PermittedUser } from './permitted-user.model';

@Injectable({ providedIn: 'any' })
export class GlobalPermissionService {
    constructor(
        @Inject(BACKEND_SERVER_API_URL_TOKEN) private BACKEND_SERVER_API_URL,
        private _httpClient: HttpClient
    ) {}

    public getGroupsWithAnyPermission(pageable: Pageable): Observable<Page<PermittedGroup>> {
        return this._httpClient
            .get<Page<PermittedGroup>>(this.BACKEND_SERVER_API_URL + 'admin/permissions/groups', {
                params: pageable.httpParams(),
            })
            .pipe(Page.mapOf(pageable));
    }

    public getGroupsWithoutAnyPermission(pageable: Pageable): Observable<Page<string>> {
        return this._httpClient
            .get<Page<string>>(this.BACKEND_SERVER_API_URL + 'admin/permissions/groups/none', {
                params: pageable.httpParams(),
            })
            .pipe(Page.mapOf(pageable));
    }

    public getUsersWithAnyPermission(pageable: Pageable): Observable<Page<PermittedUser>> {
        return this._httpClient
            .get<Page<PermittedUser>>(this.BACKEND_SERVER_API_URL + 'admin/permissions/users', {
                params: pageable.httpParams(),
            })
            .pipe(Page.mapOf(pageable));
    }

    public getUsersWithoutAnyPermission(pageable: Pageable): Observable<Page<User>> {
        return this._httpClient
            .get<Page<User>>(this.BACKEND_SERVER_API_URL + 'admin/permissions/users/none', {
                params: pageable.httpParams(),
            })
            .pipe(Page.mapOf(pageable));
    }

    public setPermissionForGroups(permission: string, ...groupNames: string[]): Observable<unknown> {
        let params = new HttpParams().set('permission', permission);
        groupNames.forEach((group) => (params = params.append('name', group)));
        return this._httpClient.put(this.BACKEND_SERVER_API_URL + 'admin/permissions/groups', {}, { params });
    }

    public setPermissionForUsers(permission: string, ...usernames: string[]): Observable<unknown> {
        let params = new HttpParams().set('permission', permission);
        usernames.forEach((user) => (params = params.append('name', user)));
        return this._httpClient.put(this.BACKEND_SERVER_API_URL + 'admin/permissions/users', {}, { params });
    }

    public revokePermissionsForGroup(groupName: string): Observable<unknown> {
        return this._httpClient.delete(this.BACKEND_SERVER_API_URL + 'admin/permissions/groups', {
            params: new HttpParams().set('name', groupName),
        });
    }

    public revokePermissionsForUser(username: string): Observable<unknown> {
        return this._httpClient.delete(this.BACKEND_SERVER_API_URL + 'admin/permissions/users', {
            params: new HttpParams().set('name', username),
        });
    }
}
