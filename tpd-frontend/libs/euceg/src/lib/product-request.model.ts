import { Observable } from 'rxjs';

import { ProductPirStatus, ProductStatus, ProductType } from './typing';
import { SubmissionList } from './submission-list.model';
import { Enumerable, HateoasResource, Page, Pageable } from '@devacfr/util';
import { EcigProduct, TobaccoProduct } from '.';
import { Auditable } from '@devacfr/core';

export class ProductRequest extends HateoasResource implements Auditable {
    constructor(
        public id: string,
        public internalProductNumber: string,
        public productNumber: string,
        public generalComment: string,
        public previousProductNumber: string,
        public product: TobaccoProduct | EcigProduct,
        public productType: ProductType,
        public readOnly: boolean,
        public sendable: boolean,
        public status: ProductStatus,
        public pirStatus: ProductPirStatus,
        public submissionType: string,
        public submitterId: string,
        public sourceFilename: string,
        public attachments: string[],
        public lastModifiedBy: string,
        public lastModifiedDate: Date,
        public createdBy: string,
        public createdDate: Date
    ) {
        super();
    }

    @Enumerable(false)
    public readonly submissions!: (pageable: Pageable) => Observable<Page<SubmissionList>>;

    @Enumerable(false)
    public readonly latestSubmission!: () => Observable<SubmissionList>;

    @Enumerable(false)
    public readonly child!: () => Observable<ProductRequest>;
}
