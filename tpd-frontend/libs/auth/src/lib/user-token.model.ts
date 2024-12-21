import { Injectable } from '@angular/core';
import { Principal } from './principal.model';

@Injectable()
export class UserToken {
    public currentUser: Principal | undefined;
}
