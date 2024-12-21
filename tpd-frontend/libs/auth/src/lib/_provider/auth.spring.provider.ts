import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthProvider } from '../auth.provider';
import { Credentials } from '../credentials.model';
import { Principal } from '../principal.model';

@Injectable()
export class AuthSpringProvider implements AuthProvider {
    constructor(private _http: HttpClient) {}

    public authenticate(credential: Credentials): Observable<Principal | null> {
        const data =
            'j_username=' +
            encodeURIComponent(credential.username) +
            '&j_password=' +
            encodeURIComponent(credential.password) +
            '&_spring_security_remember_me=' +
            credential.rememberMe +
            '&submit=Login';
        return this._http.post<Principal>('auth/login', data, {
            headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded'),
        });
    }

    public logout(): Observable<HttpResponse<unknown>> {
        // logout from the server
        return this._http.post('auth/logout', {}, { observe: 'response' });
    }

    public createToken(credential: Credentials): string {
        return window.btoa(credential.username + ':' + credential.password);
    }

    public getLocalPrincipal(): Principal | null {
        return null;
    }
}
