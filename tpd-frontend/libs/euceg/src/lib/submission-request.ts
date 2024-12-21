import { Enumerable, HateoasResource } from '@devacfr/util';
import { Observable } from 'rxjs';
import { ProductType, SendType, SubmissionStatus } from './typing';
import { TableOptions } from '@devacfr/layout';

import { ProductRequest } from './product-request.model';
import { ReceiptRequest } from './receipt-request-model';
import { SubmitterRequest } from './submitter-request.model';
import { Auditable } from '@devacfr/core';

export class SubmissionRequest extends HateoasResource implements Auditable {
    public static tableOptions: TableOptions = {
        columns: [
            {
                name: 'productId',
                sort: true,
                i18n: 'submissions.fields.productId',
            },
            {
                name: 'product.productNumber',
                sort: true,
                class: 'd-none d-xl-table-cell',
                i18n: 'submissions.fields.productNumber',
            },
            {
                name: 'productType',
                sort: true,
                class: 'd-none d-xl-table-cell',
                i18n: 'submissions.fields.productType',
            },
            {
                name: 'previousProductId',
                sort: false,
                class: 'd-none d-xl-table-cell',
                i18n: 'submissions.fields.previousProductId',
            },
            {
                name: 'nationalMarketNames',
                sort: false,
                class: 'd-none d-xl-table-cell',
                i18n: 'submissions.fields.nationalMarkets',
            },
            {
                name: 'submissionType',
                sort: true,
                i18n: 'submissions.fields.submissionType',
            },
            {
                name: 'submissionStatus',
                sort: true,
                i18n: 'submissions.fields.submissionStatus',
            },
            {
                name: 'lastModifiedDate',
                sort: true,
                i18n: 'submissions.fields.lastModifiedDate',
            },
            {
                name: 'action',
                sort: false,
                i18n: 'submissions.fields.action',
                align: 'center',
            },
        ],
    };

    constructor(
        public id: number,
        public productId: string,
        public productType: ProductType,
        public submissionStatus: SubmissionStatus,
        public submissionType: number,
        public submitterId: string,
        public sendType: SendType,
        public submission: any,
        public error: boolean,
        public progress: number,
        public attachments: any,
        public latest: boolean,
        public createdBy: string,
        public createdDate: Date,
        public lastModifiedBy: string,
        public lastModifiedDate: Date
    ) {
        super();
    }

    @Enumerable(false)
    public readonly product!: () => Observable<ProductRequest>;

    @Enumerable(false)
    public readonly receipts!: () => Observable<ReceiptRequest[]>;

    @Enumerable(false)
    public readonly submitter!: () => Observable<SubmitterRequest>;
}
