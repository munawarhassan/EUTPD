import { IsActiveMatchOptions } from '@angular/router';
import { Breakpoint, PartialRecord } from '@devacfr/util';

export type PopperPlacement =
    | 'right'
    | 'auto'
    | 'auto-start'
    | 'auto-end'
    | 'top'
    | 'bottom'
    | 'left'
    | 'top-start'
    | 'top-end'
    | 'bottom-start'
    | 'bottom-end'
    | 'right-start'
    | 'right-end'
    | 'left-start'
    | 'left-end'
    | undefined;

export type MenuTriggerType = 'click' | 'hover';
export type MenuType = 'accordion' | 'dropdown';

export interface _MenuItem {
    type?: 'root' | 'item' | 'separator';
    url?: string;
    routerLinkActiveOptions?:
        | {
              exact: boolean;
          }
        | IsActiveMatchOptions;
    trigger?: MenuTriggerType | PartialRecord<Breakpoint, MenuTriggerType>;
    permission?: string;
    iconClass?: string;
    subMenuClass?: string;
    itemClass?: string;
    linkClass?: string;
    icon?: string;
    translate?: string;
    placement?: PopperPlacement;
    dismiss?: boolean;
    items?: MenuItemType[];
}

export type MenuRootItemType = MenuRootItem | MenuSeparator;
export type MenuItemType = MenuItem;
export interface MenuRootItem extends _MenuItem {
    title: string;
    type?: 'root';
    disabled?: boolean;
}

export interface MenuItem extends _MenuItem {
    title: string;
    type?: 'item';
    bullet?: 'dot' | 'line';
}

export interface MenuSeparator {
    type: 'separator';
    class?: string;
}

export interface MenuComponentType {
    type: MenuType;
    items?: MenuRootItemType[];
    override?: {
        iconClass?: string;
        subMenuClass?: string;
        itemClass?: string;
        linkClass?: string;
        trigger?: MenuTriggerType | PartialRecord<Breakpoint, MenuTriggerType>;
        subTrigger?: MenuTriggerType | PartialRecord<Breakpoint, MenuTriggerType>;
    };
}
export interface MenuConfig {
    header: MenuComponentType;
    [component: string]: MenuComponentType;
}
