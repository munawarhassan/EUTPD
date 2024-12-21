import { FormBuilder, FormGroup, Validators } from '@angular/forms';

// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace CreateAdmin {
    export function createFormGroup(formBuilder: FormBuilder): FormGroup {
        return formBuilder.group(
            {
                login: [
                    '',
                    [
                        Validators.required,
                        Validators.minLength(3),
                        Validators.maxLength(20),
                        Validators.pattern('[a-z0-9]*'),
                    ],
                ],
                password: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(50)]],
                email: [
                    '',
                    [Validators.required, Validators.minLength(5), Validators.maxLength(100), Validators.email],
                ],
            },
            { updateOn: 'blur' }
        );
    }
}

export interface CreateAdmin {
    login: string;
    password: string;
    email: string;
}
