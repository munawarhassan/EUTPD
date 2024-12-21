import { CommonModule } from '@angular/common';
import { ModuleWithProviders, NgModule, Provider } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AuthModule } from '@devacfr/auth';
import { BreadcrumbModule, DirectivesModule, InlineSVGModule } from '@devacfr/bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { MenuModule } from './component/menu';
import { DefaultLayoutConfig } from './layout-config-default';
import { LayoutComponent } from './layout.component';
import { AsideFooterDirective } from './layout/aside/aside-footer.directive';
import { AsideComponent } from './layout/aside/aside.component';
import { AsideDirective } from './layout/aside/aside.directive';
import { ContentComponent } from './layout/content/content.component';
import { FooterDirective } from './layout/footer/footer.directive';
import { HeaderComponent } from './layout/header/header.component';
import { MenuHeaderComponent } from './layout/menu-header/menu-header.component';
import { LayoutScrollTopComponent } from './layout/scroll-top/scroll-top.component';
import { ToolbarComponent } from './layout/toolbar/toolbar.component';
import { TopbarDirective } from './layout/topbar/topbar.directive';
import { I18nService, LayoutService, LAYOUT_CONFIG, LAYOUT_CONFIG_STORAGE_KEY } from './service';
import { EventManager } from './service/event';
import { NotifierService } from './service/notifier.service';

export interface LayoutModuleConfig {
    layoutConfig?: Provider;
    name?: Provider;
}
@NgModule({
    declarations: [
        LayoutComponent,
        ContentComponent,
        AsideDirective,
        AsideComponent,
        AsideFooterDirective,
        FooterDirective,
        MenuHeaderComponent,
        ToolbarComponent,
        HeaderComponent,
        LayoutScrollTopComponent,
        TopbarDirective,
    ],
    imports: [
        CommonModule,
        RouterModule,
        TranslateModule,
        AuthModule,
        InlineSVGModule,
        MenuModule,
        DirectivesModule,
        BreadcrumbModule,
    ],
    exports: [
        LayoutComponent,
        AsideDirective,
        AsideComponent,
        AsideFooterDirective,
        ContentComponent,
        ToolbarComponent,
        FooterDirective,
        TopbarDirective,
    ],
})
export class LayoutModule {
    public static forRoot(config: LayoutModuleConfig = {}): ModuleWithProviders<LayoutModule> {
        return {
            ngModule: LayoutModule,
            providers: [
                config.layoutConfig || {
                    provide: LAYOUT_CONFIG,
                    useValue: DefaultLayoutConfig,
                },
                config.name || {
                    provide: LAYOUT_CONFIG_STORAGE_KEY,
                    useValue: name,
                },
                LayoutService,
                I18nService,
                NotifierService,
                EventManager,
            ],
        };
    }
}
