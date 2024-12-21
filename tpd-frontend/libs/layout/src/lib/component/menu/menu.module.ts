import { CommonModule } from '@angular/common';
import { ModuleWithProviders, NgModule, Provider } from '@angular/core';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { InlineSVGModule } from '@devacfr/bootstrap';
import { DEFAULT_MENU_CONFIG, MenuConfigService, MENU_CONFIG } from './menu-config.service';
import { MenuComponent } from './menu.component';
import { MenuDirective } from './menu.directive';
import { AuthModule } from '@devacfr/auth';

export interface MenuModuleConfig {
    menuConfig?: Provider;
}
@NgModule({
    imports: [CommonModule, RouterModule, TranslateModule, InlineSVGModule, AuthModule],
    exports: [MenuComponent, MenuDirective],
    declarations: [MenuComponent, MenuDirective],
})
export class MenuModule {
    public static forRoot(config: MenuModuleConfig = {}): ModuleWithProviders<MenuModule> {
        return {
            ngModule: MenuModule,
            providers: [
                config.menuConfig || {
                    provide: MENU_CONFIG,
                    useValue: DEFAULT_MENU_CONFIG,
                },
                MenuConfigService,
            ],
        };
    }
}
