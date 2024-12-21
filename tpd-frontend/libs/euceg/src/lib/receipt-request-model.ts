import { HateoasResource } from '@devacfr/util';
import { PayloadTypeString, TransmitStatusString } from './typing';

export class ReceiptRequest extends HateoasResource {
    constructor(
        public name: string,
        public messageId: string,
        public status: TransmitStatusString,
        public type: PayloadTypeString,
        public submissionId: number,
        public response: unknown,
        public error: boolean,
        public errorDetails: { code: string; message: string }[]
    ) {
        super();
    }
}
