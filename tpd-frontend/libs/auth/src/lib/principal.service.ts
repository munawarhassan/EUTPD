import { Injectable } from '@angular/core';
import { Observable, of, Subject } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { AuthProvider } from '.';
import { AuthClient } from './auth-client';
import { Principal } from './principal.model';
import { UserToken } from './user-token.model';

@Injectable({
    providedIn: 'root',
})
export class PrincipalService {
    /**  */
    private authenticationState$ = new Subject<Principal | undefined>();

    constructor(private _authClient: AuthClient, private _provider: AuthProvider, private _userToken: UserToken) {}

    public authenticate(identity: Principal) {
        this.userIdentity = identity;
        this.authenticationState$.next(this.userIdentity);
    }

    public get userIdentity(): Principal | undefined {
        return this._userToken.currentUser;
    }

    protected set userIdentity(value: Principal | undefined) {
        this._userToken.currentUser = value;
    }

    public clear() {
        this.userIdentity = undefined;
        this._userToken.currentUser = undefined;
        this.authenticationState$.next(this.userIdentity);
    }

    public get userToken(): UserToken {
        return this._userToken;
    }

    public identity(force?: boolean): Observable<Principal | undefined> {
        if (force === true) {
            this.userIdentity = undefined;
        }

        // check and see if we have retrieved the userIdentity data from the server.
        // if we have, reuse it by immediately resolving
        if (this.userIdentity) {
            this.authenticationState$.next(this.userIdentity);
            return of(this.userIdentity);
        }

        // retrieve the userIdentity data from the server, update the identity object, and then resolve.
        return this._authClient.currentUser().pipe(
            tap((principal) => {
                if (principal) {
                    this.userIdentity = principal;
                    this.userIdentity.token = this._provider.getLocalPrincipal()?.token;
                    this.authenticationState$.next(this.userIdentity);
                } else {
                    this.userIdentity = undefined;
                    this.authenticationState$.error('principal not found');
                }
            }),
            catchError((err) => {
                this.userIdentity = undefined;
                this.authenticationState$.error(err);
                return of(undefined);
            })
        );
    }

    public isAuthenticated(): boolean {
        return this.userIdentity != null;
    }

    public observable(): Observable<Principal | undefined> {
        return this.authenticationState$;
    }
}
