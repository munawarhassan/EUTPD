import { FormBuilder, FormGroup, Validators } from '@angular/forms';

// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace CreateUser {
    export function createForm(_fb: FormBuilder, user?: Partial<CreateUser>): FormGroup {
        const u: Partial<CreateUser> = user || {};
        const grp = _fb.group({
            username: [
                u.username,
                [
                    Validators.required,
                    Validators.minLength(3),
                    Validators.maxLength(20),
                    Validators.pattern('[a-z0-9]*'),
                ],
            ],
            password: [u.password, []],
            displayName: [u.displayName, [Validators.required]],
            emailAddress: [
                u.emailAddress,
                [Validators.required, Validators.email, Validators.minLength(5), Validators.maxLength(100)],
            ],
            directory: [u.directory, [Validators.required]],
            addToDefaultGroup: [u.addToDefaultGroup],
            notify: [u.notify],
        });
        return grp;
    }
}

export interface CreateUser {
    username: string;
    password: string;
    displayName: string;
    emailAddress: string;
    directory: string;
    addToDefaultGroup: boolean;
    notify: boolean;
}
