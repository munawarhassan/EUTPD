import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AuthClient, AuthModule } from '@devacfr/auth';
import { AccordionModule, BreadcrumbModule, InlineSVGModule } from '@devacfr/bootstrap';
import { Select2Module } from '@devacfr/forms';
import { LayoutModule, MenuModule, MENU_CONFIG } from '@devacfr/layout';
import { LoadingBarModule } from '@ngx-loading-bar/core';
import { LoadingBarRouterModule } from '@ngx-loading-bar/router';
import { TranslateModule } from '@ngx-translate/core';
import { DefaultMenuConfig } from '../_config/menu.config';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ExampleViewModule } from './pages/example-view/example-view.module';
import { UserService } from './user.service';

@NgModule({
    declarations: [AppComponent],
    imports: [
        // external
        BrowserModule,
        BrowserAnimationsModule,
        HttpClientModule,
        TranslateModule.forRoot(),
        // internal
        ExampleViewModule,
        AppRoutingModule,
        InlineSVGModule.forRoot({
            baseUrl: '/assets/media/',
        }),
        MenuModule.forRoot({
            menuConfig: {
                provide: MENU_CONFIG,
                useValue: DefaultMenuConfig,
            },
        }),
        AccordionModule.forRoot(),
        LayoutModule.forRoot(),
        Select2Module.forRoot(),
        AuthModule.forRoot({
            authClient: {
                provide: AuthClient,
                useExisting: UserService,
            },
        }),
        LoadingBarModule,
        LoadingBarRouterModule,
        BreadcrumbModule.forRoot(),
    ],
    providers: [UserService],
    bootstrap: [AppComponent],
})
export class AppModule {}
