import { HateoasResource } from '@devacfr/util';

export class AttachmentRevision extends HateoasResource {
    constructor(
        public id: number,
        public version: number,
        public attachmentId: string,
        public confidential: boolean = true,
        public filename: string,
        public contentType: string,
        public createdBy: string,
        public createdDate: Date,
        public lastModifiedBy: string,
        public lastModifiedDate: Date
    ) {
        super();
    }
}
