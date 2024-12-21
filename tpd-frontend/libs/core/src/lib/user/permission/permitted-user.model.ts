import { User } from '../typing';

export interface PermittedUser {
    permission: string;
    user: User;
}
