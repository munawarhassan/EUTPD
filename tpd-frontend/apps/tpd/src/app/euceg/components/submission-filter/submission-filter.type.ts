import { QueryOperator } from '@devacfr/util';

export type FilterSubmissionType = {
    name: string;
    value: string;
    property: string;
    filter: string | (string | undefined)[];
    op: QueryOperator;
    readonly?: boolean;
    not?: boolean;
};
