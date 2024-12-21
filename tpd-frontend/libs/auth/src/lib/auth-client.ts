import { HttpResponse } from '@angular/common/http';
import { Credentials } from './credentials.model';
import { Observable } from 'rxjs';
import { Principal } from './principal.model';

export abstract class AuthClient {
    public abstract signIn(credential: Credentials): Observable<HttpResponse<Principal>>;

    public abstract logout(): Observable<HttpResponse<unknown>>;

    public abstract currentUser(): Observable<Principal>;
}
