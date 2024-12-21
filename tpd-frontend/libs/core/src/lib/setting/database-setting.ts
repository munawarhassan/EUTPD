import { FormBuilder, FormGroup, Validators } from '@angular/forms';

export interface DatabaseConnection {
    driverClassName: string;
    url: string;
    user: string;
    password: string;
    properties: string[];
    passwordSet: boolean;
}

export interface DatabaseConfig {
    name: string;
    type: string;
    supportLevel: string;
    minorVersion: number;
    majorVersion: number;
    patchVersion: number;
    version: number[];
    clusterable: boolean;
    internal: boolean;
}

export interface DatabaseType {
    key: string;
    displayName: string;
    defaultDatabaseName: string;
    defaultHostName: string;
    defaultPort: number;
    defaultUserName: string;
    driverClassName: string;
    protocol: string;
    usesSid: boolean;
}

// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace DatabaseSetting {
    export function createFormGroup(formBuilder: FormBuilder): FormGroup {
        return formBuilder.group({
            type: ['', [Validators.required]],
            databaseName: ['', [Validators.required]],
            hostname: ['', [Validators.required, Validators.maxLength(255)]],
            port: ['', [Validators.required]],
            username: ['', [Validators.required, Validators.maxLength(255)]],
            password: [''],
        });
    }
}

export interface DatabaseSetting {
    type: DatabaseType | string;
    databaseName: string;
    hostname: string;
    port: number;
    username: string;
    password: string;
}
