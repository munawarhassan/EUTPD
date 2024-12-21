import { HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { AuthProvider } from './auth.provider';
import { Credentials } from './credentials.model';
import { Principal } from './principal.model';
import { PrincipalService } from './principal.service';

@Injectable({
    providedIn: 'root',
})
export class AuthService {
    public static TOKEN_KEY_STORAGE = 'authenticationToken';

    constructor(public prinpalService: PrincipalService, private _authProvider: AuthProvider) {}

    public authenticate(credential: Credentials): Observable<Principal | undefined> {
        return this._authProvider.authenticate(credential).pipe(switchMap(() => this.prinpalService.identity(true)));
    }

    public logout(): Observable<HttpResponse<unknown>> {
        this.prinpalService.clear();
        return this._authProvider.logout();
    }
}
