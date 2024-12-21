import { Injectable, inject } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable, of } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { AuthenticateGuard } from './auth.guard';

@Injectable({
    providedIn: 'root',
})
export class AdminAuthGuard extends AuthenticateGuard {
    public static canLoad(): any {
        return () => inject(AdminAuthGuard).canLoad();
    }

    public static canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): any {
        return inject(AdminAuthGuard).canActivate(route, state);
    }

    public canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
        return super.canActivate(route, state).pipe(switchMap(this._isAdminUser.bind(this)));
    }

    public canLoad(): Observable<boolean> {
        return super.canLoad().pipe(switchMap(this._isAdminUser.bind(this)));
    }

    private _isAdminUser(athenticated: boolean): Observable<boolean> {
        return athenticated ? this._permissions.isAdminUser(this._token) : of(false);
    }
}
