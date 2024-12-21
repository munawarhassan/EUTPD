import { InjectionToken } from '@angular/core';

export * from './auth.module';
export * from './auth.provider';
export * from './auth.service';
export * from './credentials.model';
export * from './principal.model';
export * from './principal.service';
export * from './permissions.service';
export * from './register-user.model';
export * from './_guard/admin-auth.guard';
export * from './_guard/auth.guard';
export * from './_guard/sysadmin-auth.guard';
export * from './_guard/user-auth.guard';
export * from './_provider/auth.jwt.provider';
export * from './_provider/auth.spring.provider';
export * from './auth-client';
export * from './user-token.model';
export * from './component/secured-image.component';
export * from './directive/authenticated.directive';
export * from './directive/pre-authorized.directive';
export * from './directive/secured-link.directive';
export * from './pipe/auth.pipe';

export const AUTH_PROVIDER = new InjectionToken('AUTH_PROVIDER');
