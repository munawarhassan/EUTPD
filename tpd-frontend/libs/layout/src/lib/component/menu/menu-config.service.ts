import { Inject, Injectable, InjectionToken } from '@angular/core';
import { MenuComponentType, MenuItem, MenuItemType, MenuRootItem, MenuRootItemType } from '.';
import { objectPath } from '@devacfr/util';
import { BehaviorSubject, Observable } from 'rxjs';
import { MenuConfig } from './menu-config';

export const MENU_CONFIG = new InjectionToken<MenuConfig>('MENU_CONFIG');

export const DEFAULT_MENU_CONFIG: MenuConfig = {
    header: {
        type: 'accordion',
    },
};

@Injectable({
    providedIn: 'root',
})
export class MenuConfigService {
    private _menuUpdated$: BehaviorSubject<MenuConfig>;

    public static applyMenuDefault(configMenu: MenuComponentType): MenuComponentType {
        const menus = configMenu.items;
        if (!menus) {
            return configMenu;
        }
        const items = menus.map((menu) => {
            let m = menu;
            if (menu.type !== 'separator') {
                m = {
                    ...({
                        type: 'root',
                        trigger: menu.items ? 'click' : undefined,
                        placement: menu.items ? 'bottom-start' : undefined,
                        dismiss: menu.items ? true : false,
                        iconClass: configMenu.override?.iconClass,
                        itemClass: configMenu.override?.itemClass,
                        subMenuClass: configMenu.override?.subMenuClass,
                        linkClass: configMenu.override?.linkClass,
                    } as MenuRootItem),
                    ...menu,
                };
                m.items = this.applyItemDefault(configMenu, menu.items);
            }

            return m;
        });
        configMenu.items = items;
        configMenu.override = configMenu.override || {};
        return configMenu;
    }

    public static applyItemDefault(
        configMenu: MenuComponentType,
        items: MenuItemType[] | undefined
    ): MenuItemType[] | undefined {
        if (!items) {
            return items;
        }
        return items.map((item) => {
            const trigger = configMenu.type === 'dropdown' && item.items ? { default: 'click', lg: 'hover' } : 'click';
            const tmp = {
                ...({
                    type: 'item',
                    trigger,
                    bullet: 'dot',
                    iconClass: configMenu.override?.iconClass,
                    itemClass: configMenu.override?.itemClass,
                    subMenuClass: configMenu.override?.subMenuClass,
                    linkClass: configMenu.override?.linkClass,
                    placement: 'right-start',
                    dismiss: item.items ? true : false,
                } as MenuItem),
                ...item,
            };
            if (item.items) {
                tmp.items = this.applyItemDefault(configMenu, item.items);
            }

            return tmp;
        });
    }

    constructor(@Inject(MENU_CONFIG) private _config: MenuConfig) {
        const config = Object.assign({}, this._config);
        for (const key in config) {
            if (Object.prototype.hasOwnProperty.call(config, key)) {
                const element = config[key];
                MenuConfigService.applyMenuDefault(element);
            }
        }

        this._menuUpdated$ = new BehaviorSubject<MenuConfig>(config);
    }

    public menuUpdated(): Observable<MenuConfig> {
        return this._menuUpdated$;
    }

    public get config(): MenuConfig {
        return this._menuUpdated$.value;
    }

    public set config(config: MenuConfig) {
        this._menuUpdated$.next(config);
    }

    public get(path: string): unknown {
        // if path is specified, get the value within object
        return objectPath.get(this.config, path);
    }
}
