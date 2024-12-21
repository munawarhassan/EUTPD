import { Injectable, inject } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable, of } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { AuthenticateGuard } from './auth.guard';

@Injectable({
    providedIn: 'root',
})
export class UserAuthGuard extends AuthenticateGuard {
    public static canLoad(): any {
        return inject(UserAuthGuard).canLoad();
    }

    public static canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): any {
        return inject(UserAuthGuard).canActivate(route, state);
    }

    public canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
        return super.canActivate(route, state).pipe(switchMap(this._isUser.bind(this)));
    }

    public canLoad(): Observable<boolean> {
        return super.canLoad().pipe(switchMap(this._isUser.bind(this)));
    }

    private _isUser(athenticated: boolean): Observable<boolean> {
        return athenticated ? this._permissions.isUser(this._token) : of(false);
    }
}
