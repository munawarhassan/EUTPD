import { Auditable } from '@devacfr/core';
import { HateoasResource } from '@devacfr/util';
import { SubmitterData, SubmitterDetail, SubmitterStatus } from './typing';

export class SubmitterRequest extends HateoasResource implements Auditable {
    constructor(
        public id: string,
        public submitterId: string,
        public details: SubmitterDetail,
        public name: string,
        public status: SubmitterStatus,
        public submitter: SubmitterData,
        public lastModifiedBy: string,
        public lastModifiedDate: Date,
        public createdBy: string,
        public createdDate: Date
    ) {
        super();
    }
}
