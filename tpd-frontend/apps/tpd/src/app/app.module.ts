import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Injector, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { JwtModule, JWT_OPTIONS } from '@auth0/angular-jwt';
import { AuthClient, AuthJwtProvider, AuthModule, AuthProvider, AuthService } from '@devacfr/auth';
import { AccordionModule, InlineSVGModule } from '@devacfr/bootstrap';
import { CoreModule, UserService } from '@devacfr/core';
import { EucegCoreModule } from '@devacfr/euceg';
import { Select2Module } from '@devacfr/forms';
import { LayoutModule, MenuModule, MENU_CONFIG } from '@devacfr/layout';
import { LoadingBarModule } from '@ngx-loading-bar/core';
import { LoadingBarRouterModule } from '@ngx-loading-bar/router';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { ModalModule } from 'ngx-bootstrap/modal';
import { NgxWebstorageModule } from 'ngx-webstorage';
import { DefaultMenuConfig } from '../_config/menu.config';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { AboutDialogModule } from './pages/about/about.module';
import { TeamDialogModule } from './pages/team/team.module';

export function jwtOptionsFactory() {
    return {
        whitelistedDomains: [/.*/],
        authScheme: 'Bearer ',
        headerName: 'Authorization',
        tokenGetter: () => {
            return (
                localStorage.getItem(AuthService.TOKEN_KEY_STORAGE) ||
                sessionStorage.getItem(AuthService.TOKEN_KEY_STORAGE)
            );
        },
    };
}

export function createTranslateLoader(http: HttpClient) {
    return new TranslateHttpLoader(http, SERVER_URL + 'assets/i18n/', '.json');
}

@NgModule({
    declarations: [AppComponent],
    imports: [
        CommonModule,
        BrowserModule,
        BrowserAnimationsModule,
        HttpClientModule,
        // external],
        JwtModule.forRoot({
            jwtOptionsProvider: {
                provide: JWT_OPTIONS,
                useFactory: jwtOptionsFactory,
            },
        }),
        TranslateModule.forRoot({
            loader: {
                provide: TranslateLoader,
                useFactory: createTranslateLoader,
                deps: [HttpClient],
            },
        }),
        NgxWebstorageModule.forRoot(),
        ModalModule.forRoot(),
        BsDropdownModule.forRoot(),
        LoadingBarModule,
        LoadingBarRouterModule,
        // internal
        AppRoutingModule,
        CoreModule.forRoot(BACKEND_SERVER_URL, BACKEND_SERVER_API_URL),
        AuthModule.forRoot({
            authClient: {
                provide: AuthClient,
                useClass: UserService,
            },
            authProvider: {
                provide: AuthProvider,
                useClass: AuthJwtProvider,
                deps: [AuthClient, Injector],
            },
        }),
        AccordionModule.forRoot(),
        LayoutModule.forRoot(),
        Select2Module.forRoot(),
        InlineSVGModule.forRoot({
            baseUrl: '/assets/media/',
        }),
        MenuModule.forRoot({
            menuConfig: {
                provide: MENU_CONFIG,
                useValue: DefaultMenuConfig,
            },
        }),
        AboutDialogModule,
        TeamDialogModule,
        EucegCoreModule.forRoot(BACKEND_SERVER_API_URL, BACKEND_SERVER_URL),
    ],
    providers: [],
    bootstrap: [AppComponent],
})
export class AppModule {}
