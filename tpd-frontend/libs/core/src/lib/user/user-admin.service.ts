import { HttpClient, HttpParams } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Page, Pageable } from '@devacfr/util';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { BACKEND_SERVER_API_URL_TOKEN } from '../shared';
import { CreateUser } from './create-user.model';
import { Permission } from './permission';
import { CreateGroup, Group, RenameUser, UpdatePassword, UpdateUserAdmin, User } from './typing';

export interface Directory {
    name: string;
    description: string;
    passwordRequired: boolean;
}

export const UserDirectories: Directory[] = [
    {
        name: 'Internal',
        description: 'User Internal Directory',
        passwordRequired: true,
    },
    {
        name: 'InternalLdap',
        description: 'Internal with LDAP Authentification',
        passwordRequired: false,
    },
    {
        name: 'InternalActiveDirectory',
        description: 'Internal with Active Directory Authentication',
        passwordRequired: true,
    },
    {
        name: 'Ldap',
        description: 'LDAP Authentification',
        passwordRequired: false,
    },
    {
        name: 'ActiveDirectory',
        description: 'Microsoft Active Directory',
        passwordRequired: false,
    },
];

@Injectable({
    providedIn: 'root',
})
export class UserAdminService {
    private API_URL: string;
    constructor(
        @Inject(BACKEND_SERVER_API_URL_TOKEN) private BACKEND_SERVER_API_URL: string,
        private _httpClient: HttpClient
    ) {
        this.API_URL = this.BACKEND_SERVER_API_URL + 'admin';
    }

    public getDirectory(name: string): Directory | undefined {
        for (let i = UserDirectories.length - 1; i >= 0; i--) {
            const n = UserDirectories[i];
            if (n.name === name) {
                return n;
            }
        }
        return undefined;
    }

    public defaultDirectory() {
        return this.getDirectory('Internal');
    }

    public getUserDetails(username: string): Observable<User> {
        const url = this.API_URL + `/users/${username}/details`;
        return this._httpClient.get<User>(url);
    }

    public getGroupDetails(groupName: string): Observable<Group> {
        const url = this.API_URL + `/groups/${groupName}/details`;
        return this._httpClient.get<Group>(url);
    }

    public findUsers(pageable: Pageable): Observable<Page<User>> {
        return this._httpClient
            .get<Page<User>>(this.API_URL + '/users', {
                params: pageable.httpParams(),
            })
            .pipe(Page.mapOf(pageable));
    }

    public findGroups(pageable: Pageable): Observable<Page<Group>> {
        return this._httpClient
            .get<Page<Group>>(this.API_URL + '/groups', {
                params: pageable.httpParams(),
            })
            .pipe(Page.mapOf(pageable));
    }

    public findUsersInGroup(groupName: string, pageable?: Pageable): Observable<Page<User>> {
        if (!pageable) {
            pageable = Pageable.of(0, 200);
        }
        return this._httpClient
            .get<Page<User>>(this.API_URL + '/groups/more-members', {
                params: pageable.httpParams(new HttpParams().append('groupname', groupName)),
            })
            .pipe(Page.mapOf(pageable));
    }

    public findUsersNotInGroup(groupName: string, pageable: Pageable): Observable<Page<User>> {
        return this._httpClient
            .get<Page<User>>(this.API_URL + '/groups/more-non-members', {
                params: pageable.httpParams(new HttpParams().append('groupname', groupName)),
            })
            .pipe(Page.mapOf(pageable));
    }

    public findGroupsForUser(username: string, pageable?: Pageable): Observable<Page<Group>> {
        if (!pageable) {
            pageable = Pageable.of(0, 200);
        }
        return this._httpClient
            .get<Page<Group>>(this.API_URL + '/users/more-members', {
                params: pageable.httpParams(new HttpParams().append('username', username)),
            })
            .pipe(Page.mapOf(pageable));
    }

    public findOtherGroupsForUser(username: string, pageable: Pageable): Observable<Page<Group>> {
        return this._httpClient
            .get<Page<Group>>(this.API_URL + '/users/more-non-members', {
                params: pageable.httpParams(new HttpParams().append('username', username)),
            })
            .pipe(Page.mapOf(pageable));
    }

    public getHighestGlobalPermission(username: string): Observable<Permission> {
        return this._httpClient.get<Permission>(this.API_URL + '/users/highest-permission', {
            params: new HttpParams().append('username', username),
        });
    }

    public updateUser(user: UpdateUserAdmin): Observable<void> {
        return this._httpClient.put<void>(this.API_URL + '/users', user);
    }

    public createUser(user: CreateUser): Observable<User> {
        return this._httpClient.post<User>(this.API_URL + '/users', user);
    }

    public deleteUser(username: string): Observable<void> {
        return this._httpClient.delete<void>(this.API_URL + '/users', {
            params: new HttpParams().append('name', username),
        });
    }

    public updateUserPassword(passwordUpdateRequest: UpdatePassword): Observable<void> {
        return this._httpClient.put<void>(this.API_URL + '/users/credentials', passwordUpdateRequest);
    }

    public renameUser(userRenameRequest: RenameUser) {
        return this._httpClient.post(this.API_URL + '/users/rename', userRenameRequest);
    }

    public userActivation(username: string, activated: boolean): Observable<void> {
        return this._httpClient.post<void>(
            this.API_URL + '/users/activate',
            {},
            {
                params: new HttpParams().append('username', username).append('activated', activated.toString()),
            }
        );
    }

    public createGroup(group: CreateGroup): Observable<Group> {
        return this._httpClient.post<Group>(this.API_URL + '/groups', group);
    }

    public deleteGroup(groupName: string): Observable<void> {
        return this._httpClient.delete<void>(this.API_URL + '/groups', {
            params: {
                name: groupName,
            },
        });
    }

    public addUsersToGroup(groupname: string, ...users: string[]): Observable<void> {
        const groupAndUsersRequest = {
            group: groupname,
            users,
        };
        return this._httpClient.post<void>(this.API_URL + '/groups/add-users', groupAndUsersRequest);
    }

    public addUserToGroup(username: string, groupName: string): Observable<void> {
        return this._httpClient.post<void>(
            this.API_URL + '/groups/add-group',
            {},
            {
                params: {
                    username,
                    groupname: groupName,
                },
            }
        );
    }

    public addUserToGroups(userAndGroupsRequest: unknown): Observable<void> {
        return this._httpClient.post<void>(this.API_URL + '/users/add-groups', userAndGroupsRequest);
    }

    public removeUserFromGroup(username: string, groupName: string): Observable<void> {
        return this._httpClient.post<void>(
            this.API_URL + '/groups/remove-user',
            {},
            {
                params: {
                    username,
                    groupname: groupName,
                },
            }
        );
    }

    public findGroupsForDirectory(pageable: Pageable, directory: string, groupName: string): Observable<Page<string>> {
        const params = pageable.httpParams().append('name', groupName);
        return this._httpClient.get<Page<string>>(
            this.BACKEND_SERVER_API_URL + 'admin/directory/' + directory + '/groups',
            {
                params,
            }
        );
    }

    public getDirectories(): Observable<{ name: string; value: string }[]> {
        return this._httpClient.get<{ name: string; value: string }[]>(
            this.BACKEND_SERVER_API_URL + 'admin/directories'
        );
    }

    public addGroups(directory: string, ...groups: string[]): Observable<unknown> {
        return this._httpClient.post(this.BACKEND_SERVER_API_URL + 'admin/directory/groups', {
            directory,
            groups,
        });
    }
}
