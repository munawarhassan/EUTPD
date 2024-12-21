import { HateoasResource } from '@devacfr/util';
import { ProductPirStatus, ProductType, SendType, SubmissionStatus } from './typing';
import { TableOptions } from '@devacfr/layout';
import { PresentationType } from './typing';

export class SubmissionList extends HateoasResource {
    public static tableOptions: TableOptions = {
        columns: [
            {
                name: 'productId',
                sort: true,
                i18n: 'submissions.fields.productId',
            },
            {
                name: 'productNumber',
                sort: true,
                class: 'd-none d-md-table-cell',
                i18n: 'submissions.fields.productNumber',
            },
            {
                name: 'productTypeName',
                sort: true,
                class: 'd-none d-xxl-table-cell',
                i18n: 'submissions.fields.productType',
            },
            {
                name: 'previousProductId',
                sort: true,
                class: 'd-none d-xxl-table-cell',
                i18n: 'submissions.fields.previousProductId',
            },
            {
                name: 'presentations.nationalMarketName',
                sort: true,
                class: 'd-none d-xl-table-cell',
                i18n: 'submissions.fields.nationalMarkets',
            },
            {
                name: 'submissionTypeName',
                sort: true,
                i18n: 'submissions.fields.submissionType',
            },
            {
                name: 'submissionStatus',
                sort: true,
                class: 'd-none d-md-table-cell',
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
        public cancelable: boolean,
        public exportable: boolean,
        public lastModifiedDate: Date,
        public presentations: PresentationType[],
        public previousProductId: string,
        public productId: string,
        public productNumber: string,
        public productType: number,
        public progress: number,
        public sendType: SendType,
        public submissionId: number,
        public submissionStatus: SubmissionStatus,
        public submissionType: string,
        public type: ProductType,
        public latest: boolean,
        public sentBy: string,
        public pirStatus?: ProductPirStatus
    ) {
        super();
    }
}
