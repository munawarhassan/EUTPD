import { Principal } from '@devacfr/auth';

export interface CreateGroup {
    name: string;
}

export interface Group {
    name: string;
    deletable: boolean;
}

export interface UpdatePassword {
    name: string;
    password: string;
    passwordConfirm: string;
}
export interface RenameUser {
    name: string;
    newName: string;
}

export interface UpdateUserAdmin {
    name: string;
    displayName: string;
    email: string;
}

export interface UpdateUserProfile {
    username: string;
    displayName: string;
    email: string;
}

export interface UserSettings {
    langKey: string;
    avatarSource?: string;
}

export interface User extends Principal {
    username: string;
    authorities: string[];
    displayName: string;
    email: string;
    langKey: string;
    activated: boolean;
    directory: string;
    deletable: boolean;
    updatable: boolean;
    groupUpdatable: boolean;
    directoryName: string;
    avatarUrl?: string;
    contactPhone?: string;
    officeLocation?: string;
}

export interface Group {
    name: string;
    deletable: boolean;
}
