import { HateoasResource } from '@devacfr/util';
import { SubmitterStatus } from './typing';

export class SubmitterList extends HateoasResource {
    constructor(
        public address: string,
        public country: string,
        public createdBy: string,
        public createdDate: Date,
        public email: string,
        public lastModifiedBy: string,
        public lastModifiedDate: Date,
        public name: string,
        public status: SubmitterStatus,
        public phone: string,
        public sme: false,
        public submitterId: string,
        public vat: string
    ) {
        super();
    }
}
