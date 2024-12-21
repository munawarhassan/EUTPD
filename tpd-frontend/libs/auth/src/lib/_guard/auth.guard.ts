import { Injectable, inject } from '@angular/core';
import { ActivatedRouteSnapshot, NavigationExtras, Router, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { PermissionsService } from '../permissions.service';
import { UserToken } from '../user-token.model';

@Injectable({
    providedIn: 'root',
})
export class AuthenticateGuard  {
    public static canLoad(): any {
        return inject(AuthenticateGuard).canLoad();
    }

    public static canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): any {
        return inject(AuthenticateGuard).canActivate(route, state);
    }

    constructor(protected _router: Router, protected _permissions: PermissionsService, protected _token: UserToken) {}

    public canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
        return this._canActivate(state);
    }

    canLoad(): Observable<boolean> {
        return this._canActivate();
    }

    private _canActivate(state?: RouterStateSnapshot): Observable<boolean> {
        return this._permissions.isAuthenticated().pipe(
            map((authenticated) => {
                if (authenticated) return true;
                let extras: NavigationExtras | undefined;
                if (state && state.url) {
                    extras = {
                        queryParams: {
                            redirectTo: state.url,
                        },
                    };
                }
                this._router.navigate(['login'], extras);
                return false;
            })
        );
    }
}
