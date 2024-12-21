import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { AuthClient, Credentials, Principal } from '@devacfr/auth';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { UpdateUserProfile, User, UserSettings } from './typing';
import { BACKEND_SERVER_API_URL_TOKEN, BACKEND_SERVER_URL_TOKEN } from '../shared';
import { UserProfile } from './user-profile.model';

/**
 *
 */
@Injectable({
    providedIn: 'root',
})
export class UserService extends AuthClient {
    private _currentUserChanged$ = new BehaviorSubject<User | UserProfile | null>(null);

    /**
     * @param  {HttpClient} privatehttpClient
     */
    constructor(
        @Inject(BACKEND_SERVER_URL_TOKEN) private BACKEND_SERVER_URL: string,
        @Inject(BACKEND_SERVER_API_URL_TOKEN) private BACKEND_SERVER_API_URL: string,
        private _httpClient: HttpClient
    ) {
        super();
    }

    public signIn(credential: Credentials): Observable<HttpResponse<Principal>> {
        return this._httpClient.post<Principal>(this.BACKEND_SERVER_URL + 'auth', credential, {
            observe: 'response',
        });
    }

    public logout(): Observable<HttpResponse<unknown>> {
        return this._httpClient.post<void>(
            this.BACKEND_SERVER_URL + 'auth/logout',
            {},
            {
                observe: 'response',
            }
        );
    }

    public currentUser(): Observable<Principal> {
        return this.getAccount();
    }

    public emitCurrentUserChanging(user: User | UserProfile | null): void {
        this._currentUserChanged$.next(user);
    }

    public get currentUserChanged(): Observable<User | UserProfile | null> {
        return this._currentUserChanged$;
    }

    /**
     * Gets the authenticated user principal for the current session.
     */
    public getAccount(): Observable<User> {
        return this._httpClient.get<User>(this.BACKEND_SERVER_API_URL + 'secure/account');
    }

    public getProfile(): Observable<UserProfile> {
        return this._httpClient.get<UserProfile>(`${this.BACKEND_SERVER_API_URL}users`);
    }

    public getAllSessions() {
        return this._httpClient.get(this.BACKEND_SERVER_API_URL + 'secure/sessions');
    }

    public invalidateSession(serieId: string) {
        const url = this.BACKEND_SERVER_API_URL + `secure/sessions/?sessions=${serieId}`;
        return this._httpClient.delete(url);
    }

    public updateProfile(user: UpdateUserProfile): Observable<UserProfile> {
        return this._httpClient
            .post<UserProfile>(this.BACKEND_SERVER_API_URL + 'users', user)
            .pipe(tap((resp) => this.emitCurrentUserChanging(resp)));
    }

    public updateSettings(username: string, userSettings: UserSettings): Observable<UserProfile> {
        return this._httpClient
            .post<UserProfile>(this.BACKEND_SERVER_API_URL + `users/${username}/settings`, userSettings)
            .pipe(tap((resp) => this.emitCurrentUserChanging(resp)));
    }

    public updateLanguage(username: string, language: string): Observable<UserProfile> {
        return this._httpClient
            .post<UserProfile>(this.BACKEND_SERVER_API_URL + `users/${username}/language/${language}`, null)
            .pipe(tap((resp) => this.emitCurrentUserChanging(resp)));
    }

    public uploadAvatar(userSlug: string, picture: File): Observable<unknown> {
        const formData = new FormData();
        formData.append('avatar', picture);
        return this._httpClient
            .post(this.BACKEND_SERVER_API_URL + `users/${userSlug}/avatar`, formData)
            .pipe(tap(() => this.emitCurrentUserChanging(null)));
    }

    public deleteAvatar(userSlug: string): Observable<unknown> {
        return this._httpClient
            .delete(this.BACKEND_SERVER_API_URL + `users/${userSlug}/avatar`)
            .pipe(tap(() => this.emitCurrentUserChanging(null)));
    }

    public getAvatarUrl(userSlug: string, size = 32): Observable<{ name: string; href: string }> {
        const url = `rest/api/users/${userSlug}/avatar.png?s=${size}`;
        return this._httpClient
            .get<{ name: string; href: string }>(this.BACKEND_SERVER_API_URL + `users/${userSlug}/avatar`)
            .pipe(
                map((urlRef) => {
                    if (!urlRef.href) urlRef.href = url;
                    return urlRef;
                }),
                catchError(() => of({ name: userSlug, href: url }))
            );
    }

    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    public registerUser(account: unknown, callback?: unknown): Observable<unknown> {
        return of(null);
    }

    public activateAccount(key: string) {
        return this._httpClient.get(this.BACKEND_SERVER_API_URL + 'users/activate', {
            params: new HttpParams().set('key', key),
        });
    }

    public requestResetPassword(username: string): Observable<unknown> {
        return this._httpClient.get(this.BACKEND_SERVER_API_URL + 'users/request_reset_password', {
            params: new HttpParams().set('username', username),
        });
    }

    public resetPassword(token: string, password: string): Observable<unknown> {
        return this._httpClient.get(this.BACKEND_SERVER_API_URL + 'users/reset_password', {
            params: new HttpParams().set('token', token).set('password', password),
        });
    }

    public changePassword(currentPassword: string, newPassword: string): Observable<unknown> {
        const pwd = {
            currentPassword,
            newPassword,
        };
        return this._httpClient.post(this.BACKEND_SERVER_API_URL + 'users/change_password', pwd);
    }
}
