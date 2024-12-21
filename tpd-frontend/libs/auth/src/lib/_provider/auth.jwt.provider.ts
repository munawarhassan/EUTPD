import { HttpResponse } from '@angular/common/http';
import { Injectable, Injector } from '@angular/core';
import { JwtHelperService } from '@auth0/angular-jwt';
import { Observable, of } from 'rxjs';
import { switchMap, tap } from 'rxjs/operators';
import { AuthClient } from '../auth-client';
import { AuthProvider } from '../auth.provider';
import { AuthService } from '../auth.service';
import { Credentials } from '../credentials.model';
import { Principal } from '../principal.model';

@Injectable()
export class AuthJwtProvider implements AuthProvider {
    constructor(private _authClient: AuthClient, private _injector: Injector) {}

    public authenticate(credential: Credentials): Observable<Principal | null> {
        return this._authClient.signIn(credential).pipe(
            switchMap((resp) => {
                const token = this.createToken(credential, resp);
                const principal = token ? this._getPrincipal(token, resp.body) : null;
                if (principal && principal.token) {
                    this.storeAuthenticationToken(principal.token, credential.rememberMe);
                }
                return of(principal);
            })
        );
    }

    public logout(): Observable<HttpResponse<unknown>> {
        return this._authClient.logout().pipe(
            tap(() => {
                localStorage.removeItem(AuthService.TOKEN_KEY_STORAGE);
                sessionStorage.removeItem(AuthService.TOKEN_KEY_STORAGE);
            })
        );
    }

    private createToken(credential: Credentials, resp: HttpResponse<unknown>): string | null {
        if (resp.headers) {
            const bearerToken = resp.headers.get('Authorization');
            if (bearerToken && bearerToken.slice(0, 7) === 'Bearer ') {
                const jwt = bearerToken.slice(7, bearerToken.length);
                return jwt;
            }
            return null;
        } else {
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            const payload = resp as any;
            return payload.token;
        }
    }

    public getLocalPrincipal(): Principal | null {
        const token = localStorage.getItem(AuthService.TOKEN_KEY_STORAGE);
        return token ? this._getPrincipal(token, null) : null;
    }

    private _getPrincipal(token: string, principal: Principal | null): Principal | null {
        const jwtHelperService = this._injector.get(JwtHelperService);

        if (token != null && !jwtHelperService.isTokenExpired(token)) {
            const jwttoken = jwtHelperService.decodeToken(token);
            const auth = jwttoken.auth as string;
            if (!principal) {
                return {
                    username: jwttoken.sub,
                    authorities: auth ? auth.split(',') : null,
                    token,
                };
            }
            principal.token = token;
        }
        return principal;
    }

    public get token(): string | null {
        return (
            localStorage.getItem(AuthService.TOKEN_KEY_STORAGE) || sessionStorage.getItem(AuthService.TOKEN_KEY_STORAGE)
        );
    }

    private storeAuthenticationToken(token: string, rememberMe: boolean) {
        localStorage.removeItem(AuthService.TOKEN_KEY_STORAGE);
        sessionStorage.removeItem(AuthService.TOKEN_KEY_STORAGE);
        if (token == null) {
            return;
        }
        if (rememberMe) {
            localStorage.setItem(AuthService.TOKEN_KEY_STORAGE, token);
        } else {
            sessionStorage.setItem(AuthService.TOKEN_KEY_STORAGE, token);
        }
    }
}
