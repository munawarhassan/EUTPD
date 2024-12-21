import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Injector, ModuleWithProviders, NgModule, Provider } from '@angular/core';
import { AuthProvider } from './auth.provider';
import { AuthService } from './auth.service';
import { SecuredImageComponent } from './component/secured-image.component';
import { AuthenticatedDirective } from './directive/authenticated.directive';
import { PreAuthorizedDirective } from './directive/pre-authorized.directive';
import { SecuredLinkDirective } from './directive/secured-link.directive';
import { PermissionsService } from './permissions.service';
import { AuthPipe } from './pipe/auth.pipe';
import { PrincipalService } from './principal.service';
import { UserToken } from './user-token.model';
import { AuthJwtProvider } from './_provider/auth.jwt.provider';

export interface AuthModuleConfig {
    authClient: Provider;
    authProvider?: Provider;
}

@NgModule({
    imports: [CommonModule],
    exports: [PreAuthorizedDirective, AuthenticatedDirective, SecuredLinkDirective, SecuredImageComponent, AuthPipe],
    declarations: [
        PreAuthorizedDirective,
        AuthenticatedDirective,
        SecuredLinkDirective,
        SecuredImageComponent,
        AuthPipe,
    ],
    providers: [],
})
export class AuthModule {
    public static forRoot(config: AuthModuleConfig): ModuleWithProviders<AuthModule> {
        return {
            ngModule: AuthModule,
            providers: [
                config.authClient,
                config.authProvider || {
                    provide: AuthProvider,
                    useClass: AuthJwtProvider,
                    deps: [HttpClient, Injector],
                },
                AuthService,
                PrincipalService,
                PermissionsService,
                UserToken,
            ],
        };
    }
}
