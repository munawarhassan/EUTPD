import { Credentials } from './credentials.model';
import { Observable } from 'rxjs';

import { Principal } from './principal.model';
import { HttpResponse } from '@angular/common/http';

export abstract class AuthProvider {
    public abstract authenticate(credential: Credentials): Observable<Principal | null>;

    public abstract logout(): Observable<HttpResponse<unknown>>;

    public abstract getLocalPrincipal(): Principal | null;
}
