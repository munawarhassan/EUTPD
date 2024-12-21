import { HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AuthClient, Credentials, Principal } from '@devacfr/auth';
import { Observable, of } from 'rxjs';

const user: Principal = { username: 'devacfr', authorities: [] };

@Injectable({ providedIn: 'root' })
export class UserService extends AuthClient {
    public signIn(credential: Credentials): Observable<HttpResponse<Principal>> {
        throw new Error('Method not implemented.');
    }
    public logout(): Observable<HttpResponse<unknown>> {
        return of();
    }
    public currentUser(): Observable<Principal> {
        return of(user);
    }
}
