import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { EMPTY, Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable()
export class UnavailableInterceptor implements HttpInterceptor {
    constructor(private _router: Router) {}

    intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
        // Pass on the cloned request instead of the original request.
        return next.handle(req).pipe(this.handleErrors.bind(this));
    }

    handleErrors(source: Observable<HttpEvent<unknown>>): Observable<HttpEvent<unknown>> {
        return source.pipe(
            catchError((error: HttpErrorResponse) => {
                return error.status === 503 || error.status === 504 ? this.handle() : throwError(error);
            })
        );
    }

    handle() {
        this._router.navigate(['/unavailable']);
        return EMPTY;
    }
}
