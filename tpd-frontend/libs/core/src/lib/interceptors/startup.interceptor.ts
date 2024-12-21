import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import _ from 'lodash-es';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { StartupService } from '../startup/startup.service';

@Injectable({ providedIn: 'root' })
export class StartupInterceptor implements HttpInterceptor {
    constructor(private _router: Router, private _startupService: StartupService) {}

    intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
        return next.handle(req).pipe(this.handle.bind(this));
    }

    handle(source: Observable<HttpEvent<unknown>>): Observable<HttpEvent<unknown>> {
        return source.pipe(
            tap(_.noop, (error) => {
                if (error.status === 503) {
                    this.handle503();
                }
            })
        );
    }

    handle503() {
        this._startupService.handle(this._router).subscribe(_.noop);
    }
}
