import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserSettings } from './typing';

// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace UserProfile {
    export function createForm(_fb: FormBuilder): FormGroup {
        const grp = _fb.group({
            settings: _fb.group({
                langKey: [null],
                avatarSource: [null],
            }),
            avatarUrl: [null],
            username: [null],
            displayName: [null, Validators.required],
            email: [null, [Validators.required, Validators.email, Validators.minLength(5), Validators.maxLength(100)]],
            contactPhone: [null],
            officeLocation: [null],
            readOnly: [null],
        });
        return grp;
    }
}
export interface UserProfile {
    username: string;
    displayName: string;
    email: string;
    activated: boolean;
    readOnly: boolean;
    contactPhone?: string;
    officeLocation?: string;
    avatarUrl?: string;
    settings: UserSettings;
}
