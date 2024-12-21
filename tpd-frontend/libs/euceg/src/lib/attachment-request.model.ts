import { Auditable } from '@devacfr/core';
import { HateoasResource } from '@devacfr/util';
import { AttachmentAction, AttachmentSendStatus } from '.';

export class AttachmentRequest extends HateoasResource implements Auditable {
    constructor(
        public attachmentId: string,
        public filename: string,
        public size: number,
        public contentType: string,
        public confidential: boolean,
        public action: AttachmentAction,
        public sendStatus: AttachmentSendStatus,
        public createdBy: string,
        public createdDate: Date,
        public lastModifiedBy: string,
        public lastModifiedDate: Date,
        public deletable: boolean
    ) {
        super();
    }
}
