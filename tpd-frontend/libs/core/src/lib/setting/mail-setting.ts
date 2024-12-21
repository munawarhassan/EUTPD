import { FormBuilder, FormGroup, Validators } from '@angular/forms';

// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace MailSetting {
    export function createFormGroup(formBuilder: FormBuilder): FormGroup {
        return formBuilder.group({
            hostname: ['', [Validators.required]],
            port: [''],
            username: [''],
            password: [''],
            tls: [''],
            emailFrom: [
                '',
                [Validators.required, Validators.minLength(5), Validators.maxLength(100), Validators.email],
            ],
        });
    }
}

export interface MailSetting {
    hostname: string;
    port: number;
    username: string;
    password: string;
    tls: boolean;
    emailFrom: string;
}
