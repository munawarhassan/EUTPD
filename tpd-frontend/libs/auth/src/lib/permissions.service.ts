import { Injectable } from '@angular/core';
import { PrincipalService } from './principal.service';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { UserToken } from './user-token.model';

@Injectable()
export class PermissionsService {
    constructor(private _principalService: PrincipalService) {}
    /**
     *
     * @param token
     * @returns
     */
    public isUser(token: UserToken): Observable<boolean> {
        return this.hasAuthority(token, 'USER');
    }

    /**
     *
     * @param token
     * @returns
     */
    public isSystemAdminUser(token: UserToken): Observable<boolean> {
        return this.hasAuthority(token, 'SYSTEM');
    }

    /**
     *
     * @param token
     * @returns
     */
    public isAdminUser(token: UserToken): Observable<boolean> {
        return this.hasAuthority(token, 'ADMIN');
    }

    /**
     *
     * @param token
     * @returns
     */
    public isAuthenticated(): Observable<boolean> {
        return this._principalService.identity().pipe(map((principal) => principal != null));
    }

    /**
     *
     * @param token
     * @param authorities
     * @returns
     */
    public hasAnyAuthority(token: UserToken, ...authorities: string[]): boolean {
        if (!token.currentUser || !token.currentUser.authorities) {
            return false;
        }
        for (let i = 0; i < authorities.length; i++) {
            if (token.currentUser.authorities.includes(authorities[i])) {
                return true;
            }
        }

        return false;
    }

    /**
     *
     * @param token
     * @param authority
     * @returns
     */
    public hasAuthority(token: UserToken, authority: string): Observable<boolean> {
        return of(this.hasAnyAuthority(token, authority));
    }
}
