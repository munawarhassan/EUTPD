import { isArray } from 'lodash-es';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function isErrorApiResponse(resp: any): boolean {
    return resp && resp.errors && isArray(resp.errors);
}

export interface ErrorApiResponse {
    errors: {
        context: string;
        message: string;
        exceptionName: string;
        throwable: string;
    }[];
}
