import { isArray } from 'lodash-es';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function isErrorResponse(resp: any) {
    return resp && resp.failures && isArray(resp.failures);
}

export interface ErrorResponse {
    failures: {
        description: string;
    }[];
    hasFailures: boolean;
}
