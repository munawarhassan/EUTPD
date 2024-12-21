import { HateoasResource } from '@devacfr/util';
import { PresentationType, ProductPirStatus, ProductStatus, ProductType, SubmissionStatus } from './typing';

export class ProductList extends HateoasResource {
    constructor(
        public productType: ProductType,
        public productNumber: string,
        public child: string,
        public submitterId: string,
        public type: number,
        public presentations: PresentationType[],
        public submissionType: number,
        public status: ProductStatus,
        public pirStatus: ProductPirStatus,
        public readOnly: boolean,
        public sendable: boolean,
        public lastModifiedDate: Date,
        public latestSubmissionStatus: SubmissionStatus,
        public latestError: boolean,
        public attachments: string[]
    ) {
        super();
    }
}
