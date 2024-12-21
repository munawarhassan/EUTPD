import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { StartupInterceptor } from './startup.interceptor';
import { UnavailableInterceptor } from './unavailable.interceptor';

export const httpInterceptorProviders = [
    { provide: HTTP_INTERCEPTORS, useClass: StartupInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: UnavailableInterceptor, multi: true },
];
