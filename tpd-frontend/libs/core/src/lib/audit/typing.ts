export interface BaseAuditEntity {
    id: number;
    channels: string[];
    data: Record<string, unknown>;
    action:
        | 'ApplicationConfigurationChangedEvent'
        | 'GroupCreatedEvent'
        | 'GroupDeletedEvent'
        | 'GroupMemberAddedEvent'
        | 'GroupMemberRemovedEvent'
        | 'GlobalPermissionGrantedEvent'
        | 'GlobalPermissionRevokedEvent'
        | 'GlobalPermissionModifiedEvent'
        | 'UserCreatedEvent'
        | 'UserDeletedEvent'
        | 'AuthenticationSuccessEvent'
        | 'AuthenticationFailureEvent'
        | 'AttachmentActionEvent'
        | 'AttachmentMovedEvent';
    principal?: string;
    timestamp?: number;
}

export interface AuditConfigEntity extends BaseAuditEntity {
    action: 'ApplicationConfigurationChangedEvent';
    data: {
        property: string;
        newValue: unknown;
        oldValue: unknown;
    };
}

export interface AuditGroupEntity extends BaseAuditEntity {
    action: 'GroupCreatedEvent' | 'GroupDeletedEvent';
    data: {
        group: string;
    };
}

export interface AuditGroupMembershipEntity extends BaseAuditEntity {
    action: 'GroupMemberAddedEvent' | 'GroupMemberRemovedEvent';
    data: {
        username: string;
        group: string;
    };
}

export interface AuditPermissionEntity extends BaseAuditEntity {
    action: 'GlobalPermissionGrantedEvent' | 'GlobalPermissionRevokedEvent';
    data: {
        affectedGroup: string;
        affectedUser: string;
        permission: string;
    };
}

export interface AuditPermissionModifiedEntity extends BaseAuditEntity {
    action: 'GlobalPermissionModifiedEvent';
    data: {
        affectedUser: string;
        affectedGroup: string;
        oldPermission: string;
        newPermission: string;
    };
}

export interface AuditUserEntity extends BaseAuditEntity {
    action: 'UserCreatedEvent' | 'UserDeletedEvent';
    data: {
        user: string;
    };
}

export interface AuditAuthenticationSuccessEntity extends BaseAuditEntity {
    action: 'AuthenticationSuccessEvent';
    data: {
        'authentication-method': string;
    };
}

export interface AuditAuthenticationFailureEntity extends BaseAuditEntity {
    action: 'AuthenticationFailureEvent';
    data: {
        'authentication-method': string;
        error: string;
        type: string;
    };
}

export interface AuditAttachmentActionEventEntity extends BaseAuditEntity {
    action: 'AttachmentActionEvent';
    data: {
        action: 'created' | 'updated' | 'deleted';
        filename: string;
        path: string;
        mimeType?: string;
    };
}

export interface AuditAttachmentMovedEventEntity extends BaseAuditEntity {
    action: 'AttachmentMovedEvent';
    data: {
        filename: string;
        path: string;
        newPath: string;
        mimeType?: string;
    };
}

export type AuditEntity =
    | BaseAuditEntity
    | AuditConfigEntity
    | AuditGroupEntity
    | AuditGroupMembershipEntity
    | AuditPermissionEntity
    | AuditPermissionModifiedEntity
    | AuditUserEntity
    | AuditAuthenticationSuccessEntity
    | AuditAuthenticationFailureEntity
    | AuditAttachmentActionEventEntity
    | AuditAttachmentMovedEventEntity;

export interface Auditable {
    lastModifiedBy: string;
    lastModifiedDate: Date;
    createdBy: string;
    createdDate: Date;
}
