import { Injectable } from '@angular/core';
import { I18nService } from './i18n.service';
import { ErrorApiResponse, ErrorResponse, isErrorApiResponse, isErrorResponse } from '@devacfr/util';
import { EventManager } from './event';
import { HttpErrorResponse } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { ObservableInput, ObservedValueOf, OperatorFunction, throwError } from 'rxjs';

import toastr from 'toastr';

const defaultOptions = {
    closeButton: true,
    closeHtml: '<button type="button" class="toast-close-button fs-3x fw-bolder me-2" role="button">&nbsp;</button>',
    debug: false,
    newestOnTop: false,
    progressBar: true,
    positionClass: 'toast-top-right mt-15',
    preventDuplicates: true,
    onclick: undefined,
    showDuration: 3000,
    hideDuration: 1000,
    timeOut: 10000,
    extendedTimeOut: 3000,
    showEasing: 'swing',
    hideEasing: 'linear',
    showMethod: 'fadeIn',
    hideMethod: 'fadeOut',
} as ToastrOptions;

(toastr as any).options = defaultOptions;

@Injectable({
    providedIn: 'root',
})
export class NotifierService {
    constructor(private _eventManager: EventManager, private _i18nService: I18nService) {}

    public successWithKey(key: string, params?: any) {
        toastr.success(this._i18nService.instant(key, params));
    }

    public success(message: string) {
        toastr.success(message);
    }

    public error(err: string | ErrorApiResponse | ErrorResponse | HttpErrorResponse): void {
        const message = this.extractErrorMessage(err);
        message.then((msg) => {
            if (msg) toastr.error(msg);
            // this.broadcast(NotifyEvent.create(this, this._i18nService.instant(key)));
        });
    }

    public async extractErrorMessage(
        err: string | ErrorApiResponse | ErrorResponse | HttpErrorResponse
    ): Promise<string | undefined> {
        let error;
        if (err instanceof HttpErrorResponse) {
            error = err.error;
        }
        if (typeof err === 'string') {
            return err;
        }

        if (error instanceof Blob) {
            error = JSON.parse(await error.text());
        }

        if (isErrorApiResponse(error)) {
            return this.extractFirstError(error as ErrorApiResponse);
        } else if (isErrorResponse(error)) {
            return this.extractFirstError(error as ErrorResponse);
        } else {
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            const ngErr = error as any;
            // angular error
            if (ngErr && ngErr.message) {
                return ngErr.message;
            } else if (ngErr && ngErr.body && ngErr.body.error) {
                // error connection
                return `Error connection ${ngErr.statusText} (${ngErr.status}): ${ngErr.body.error}`;
            }
        }
        return undefined;
    }

    public catchError<T, O extends ObservableInput<any>>(): OperatorFunction<T, T | ObservedValueOf<O>> {
        return catchError((err) => {
            this.error(err);
            return throwError(err);
        });
    }

    private extractFirstError(data: ErrorApiResponse | ErrorResponse): string {
        const apierr = data as ErrorApiResponse;
        if (apierr.errors) {
            const errors = apierr.errors;
            return errors[0].message;
        }
        const err = data as ErrorResponse;
        if (err.hasFailures) {
            const error = err.failures[0];
            return 'Validation Error<br/>' + error.description;
        }
        return 'error unknown';
    }
}
