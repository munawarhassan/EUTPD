import {
    DataUtil,
    ElementStyleUtil,
    EventHandlerUtil,
    getAttributeValueByBreakpoint,
    getElementChild,
    getElementParents,
    getHighestZindex,
    getUniqueIdWithPrefix,
    insertAfterElement,
    slideDown,
    slideUp,
} from '@devacfr/util';
import { Instance, VirtualElement, createPopper } from '@popperjs/core';
import { MenuType } from '.';
import { PopperPlacement } from './menu-config';

export interface MenuOptions {
    dropdown: {
        hoverTimeout: number;
        zindex: number;
    };
    accordion: {
        slideSpeed: number;
        expand: boolean;
    };
}

type MenuSubscription = {
    el: HTMLElement;
    event: string;
    handler: EventListener;
};

export const DefaultMenuOptions: MenuOptions = {
    dropdown: {
        hoverTimeout: 200,
        zindex: 105,
    },
    accordion: {
        slideSpeed: 250,
        expand: false,
    },
};

export class MenuWrapper {
    private _element: HTMLElement;
    menuType: MenuType;
    options: MenuOptions;
    instanceUid: string;
    private _triggerElement: HTMLElement | undefined;

    private subscriptions: MenuSubscription[] = [];

    // public static methods
    // Get KTMenu instance by element
    public static getInstance(element: HTMLElement): MenuWrapper | undefined {
        // Element has menu DOM reference in it's DATA storage
        const elementMenu = DataUtil.get(element, 'menu') as MenuWrapper;
        if (elementMenu) {
            return elementMenu;
        }

        // Element has .menu parent
        const menu = element.closest('.menu');
        if (menu) {
            const menuData = DataUtil.get(menu as HTMLElement, 'menu') as MenuWrapper;
            if (menuData) {
                return menuData;
            }
        }

        // Element has a parent with DOM reference to .menu in it's DATA storage
        if (element.classList.contains('menu-link')) {
            const sub = element.closest('.menu-sub');
            if (sub) {
                const subMenu = DataUtil.get(sub as HTMLElement, 'menu') as MenuWrapper;
                if (subMenu) {
                    return subMenu;
                }
            }
        }

        return;
    }

    // Hide all dropdowns and skip one if provided
    public static hideDropdowns(skip: HTMLElement | undefined) {
        const items = document.querySelectorAll('.show.menu-dropdown[data-lt-menu-trigger]');

        if (items && items.length > 0) {
            for (let i = 0, len = items.length; i < len; i++) {
                const item = items[i] as HTMLElement;
                const menu = MenuWrapper.getInstance(item as HTMLElement);

                if (menu && menu.getItemSubType(item) === 'dropdown') {
                    if (skip) {
                        const el = menu.getItemSubElement(item);
                        if (el && el.contains(skip) === false && item.contains(skip) === false && item !== skip) {
                            menu.hide(item);
                        }
                    } else {
                        menu.hide(item);
                    }
                }
            }
        }
    }

    public static updateDropdowns() {
        const items = document.querySelectorAll('.show.menu-dropdown[data-lt-menu-trigger]');
        if (items && items.length > 0) {
            for (let i = 0, len = items.length; i < len; i++) {
                const item = items[i];
                const popper = DataUtil.get(item as HTMLElement, 'popper') as Instance;
                if (popper) {
                    popper.forceUpdate();
                }
            }
        }
    }

    constructor(_element: HTMLElement, menuType: MenuType, options: MenuOptions) {
        this._element = _element;
        this.menuType = menuType;
        this.options = Object.assign({}, DefaultMenuOptions, options);
        this.instanceUid = getUniqueIdWithPrefix('menu');
        // this.element.setAttribute('data-lt-menu', 'true')
        this._setTriggerElement();
        this.update();
        DataUtil.set(this._element, 'menu', this);
        return this;
    }

    public destroy(): void {
        this.subscriptions.forEach((sub) => sub.el.removeEventListener(sub.event, sub.handler));
    }

    public get triggerElement(): HTMLElement | undefined {
        return this._triggerElement;
    }

    public get element(): HTMLElement {
        return this._element;
    }

    // Set external trigger element
    private _setTriggerElement() {
        const target = document.querySelector(`[data-lt-menu-target="#${this._element.getAttribute('id')}"`);

        if (target) {
            this._triggerElement = target as HTMLElement;
        } else if (
            this._element.previousElementSibling &&
            this._element.previousElementSibling.hasAttribute('data-lt-menu-trigger')
        ) {
            this._triggerElement = this._element.previousElementSibling as HTMLElement;
        } else if (
            this._element.parentNode &&
            getElementChild(this._element.parentNode as HTMLElement, '[data-lt-menu-trigger]')
        ) {
            const child = getElementChild(this._element.parentNode as HTMLElement, '[data-lt-menu-trigger]');
            if (child) {
                this._triggerElement = child;
            }
        }

        if (this._triggerElement) {
            DataUtil.set(this._triggerElement, 'menu', this);
        }
    }

    // Test if menu has external trigger element
    private _isTriggerElement = (item: HTMLElement) => {
        return this._triggerElement === item;
    };

    // Get item option(through html attributes)
    private _getItemOption = (item: HTMLElement, name: string) => {
        let value;
        if (item && item.hasAttribute('data-lt-menu-' + name)) {
            const attr = item.getAttribute('data-lt-menu-' + name) || '';
            value = getAttributeValueByBreakpoint(attr);
            if (value !== null && String(value) === 'true') {
                value = true;
            } else if (value !== null && String(value) === 'false') {
                value = false;
            }
        }
        return value;
    };

    // Get item element
    private _getItemElement(_element: HTMLElement): Element | null {
        // Element is the external trigger element
        if (this._isTriggerElement(_element)) {
            return _element;
        }

        // Element has item toggler attribute
        if (_element.hasAttribute('data-lt-menu-trigger')) {
            return _element;
        }

        // Element has item DOM reference in it's data storage
        const itemElement = DataUtil.get(_element, 'item') as Element;
        if (itemElement) {
            return itemElement;
        }

        // Item is parent of element
        const item = _element.closest('.menu-item[data-lt-menu-trigger]');
        if (item) {
            return item;
        }

        // Element's parent has item DOM reference in it's data storage
        const sub = _element.closest('.menu-sub');
        if (sub) {
            const subItem = DataUtil.get(sub as HTMLElement, 'item') as HTMLElement;
            if (subItem) {
                return subItem;
            }
        }
        return null;
    }

    // Get item parent element
    private _getItemParentElement(item: HTMLElement): HTMLElement | null {
        const sub = item.closest('.menu-sub');
        if (!sub) {
            return null;
        }

        const subItem = DataUtil.get(sub as HTMLElement, 'item') as HTMLElement;
        if (subItem) {
            return subItem;
        }

        const parentItem = sub.closest('.menu-item[data-lt-menu-trigger]');
        if (sub && parentItem) {
            return parentItem as HTMLElement;
        }

        return null;
    }

    // Get item parent elements
    public getItemParentElements(item: HTMLElement) {
        const parents: Element[] = [];
        let parent;
        let i = 0;
        let buffer = item;

        do {
            parent = this._getItemParentElement(buffer);
            if (parent) {
                parents.push(parent);
                buffer = parent;
            }

            i++;
        } while (parent !== null && i < 20);

        if (this._triggerElement) {
            parents.unshift(this._triggerElement);
        }

        return parents;
    }

    // Prepare popper config for dropdown(see: https://popper.js.org/docs/v2/)
    public _getDropdownPopperConfig(item: HTMLElement) {
        // Placement
        const placementOption = this._getItemOption(item, 'placement');
        let placement: PopperPlacement = 'right';
        if (placementOption) {
            placement = placementOption as PopperPlacement;
        }

        // Flip
        const flipValue = this._getItemOption(item, 'flip') as string;
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        const flip = flipValue ? flipValue.split(',') : [];

        // Offset
        const offsetValue = this._getItemOption(item, 'offset') as string;
        const offset = offsetValue ? offsetValue.toString().split(',') : [];

        // Strategy
        const strategy: 'absolute' | 'fixed' = this._getItemOption(item, 'strategy') ?? 'absolute';

        const altAxis = this._getItemOption(item, 'flip') !== false;

        return {
            placement: placement,
            strategy: strategy,
            modifiers: [
                {
                    name: 'offset',
                    options: {
                        offset: offset,
                    },
                },
                {
                    name: 'preventOverflow',
                    options: {
                        altAxis: altAxis,
                    },
                },
                {
                    name: 'flip',
                    options: {
                        flipVariations: false,
                    },
                },
            ],
        };
    }

    // Get item child element
    private _getItemChildElement = (item: HTMLElement) => {
        let selector = item;

        const subItem = DataUtil.get(item, 'sub') as HTMLElement;
        if (subItem) {
            selector = subItem;
        }

        if (selector) {
            //element = selector.querySelector('.show.menu-item[data-lt-menu-trigger]');
            const element = selector.querySelector('.menu-item[data-lt-menu-trigger]');
            if (element) {
                return element;
            }
        }
        return null;
    };

    // Get item child elements
    private _getItemChildElements = (item: HTMLElement) => {
        const children: Element[] = [];
        let child;
        let i = 0;
        let buffer = item;
        do {
            child = this._getItemChildElement(buffer);
            if (child) {
                children.push(child);
                buffer = child as HTMLElement;
            }

            i++;
        } while (child !== null && i < 20);

        return children;
    };

    // Get item sub element
    public getItemSubElement(item: HTMLElement): HTMLElement | null {
        if (!item) {
            return null;
        }

        if (this._isTriggerElement(item)) {
            return this._element;
        }

        if (item.classList.contains('menu-sub')) {
            return item;
        } else if (DataUtil.has(item, 'sub')) {
            return DataUtil.get(item, 'sub') as HTMLElement;
        } else {
            return getElementChild(item, '.menu-sub');
        }
    }

    private _getCss = (el: HTMLElement, styleProp: string) => {
        const defaultView = (el.ownerDocument || document).defaultView;
        if (!defaultView) {
            return '';
        }

        // sanitize property name to css notation
        // (hyphen separated words eg. font-Size)
        styleProp = styleProp.replace(/([A-Z])/g, '-$1').toLowerCase();

        return defaultView.getComputedStyle(el, null).getPropertyValue(styleProp);
    };

    // Get item sub type
    public getItemSubType(element: HTMLElement): MenuType {
        const sub = this.getItemSubElement(element);
        if (sub && parseInt(this._getCss(sub, 'z-index')) > 0) {
            return 'dropdown';
        } else {
            return 'accordion';
        }
    }

    // Test if item's sub is shown
    public isItemSubShown(item: HTMLElement) {
        const sub = this.getItemSubElement(item);
        if (sub) {
            if (this.getItemSubType(item) === 'dropdown') {
                return sub.classList.contains('show') && sub.hasAttribute('data-popper-placement');
            } else {
                return item.classList.contains('show');
            }
        }

        return false;
    }

    // Test if item dropdown is permanent
    public isItemDropdownPermanent(item: HTMLElement) {
        return this._getItemOption(item, 'permanent') === true;
    }

    // Test if item's parent is shown
    public isItemParentShown(item: HTMLElement) {
        return getElementParents(item, '.menu-item.show').length > 0;
    }

    // Test of it is item sub element
    private _isItemSubElement(item: HTMLElement) {
        return item.classList.contains('menu-sub');
    }

    // Test if item has sub
    private _hasItemSub = (item: HTMLElement) => {
        return item.classList.contains('menu-item') && item.hasAttribute('data-lt-menu-trigger');
    };

    // Get link element
    public getItemLinkElement(item: HTMLElement) {
        return getElementChild(item, '.menu-link');
    }

    // Get toggle element
    public getItemToggleElement(item: HTMLElement) {
        if (this._triggerElement) {
            return this._triggerElement;
        }

        return this.getItemLinkElement(item);
    }

    // Show item dropdown
    private _showDropdown(item: HTMLElement) {
        if (EventHandlerUtil.trigger(this._element, 'show.lt.menu') === false) {
            return;
        }

        // Hide all currently shown dropdowns except current one
        MenuWrapper.hideDropdowns(item);

        // const toggle = this._isTriggerElement(item) ? item : this._getItemLinkElement(item);
        const sub = this.getItemSubElement(item);
        if (!sub) {
            return;
        }
        const width = this._getItemOption(item, 'width') as string;
        const height = this._getItemOption(item, 'height') as string;

        let zindex = this.options.dropdown.zindex;
        const parentZindex = getHighestZindex(item); // update
        // Apply a new z-index if dropdown's toggle element or it's parent has greater z-index // update
        if (parentZindex !== null && parentZindex >= zindex) {
            zindex = parentZindex + 1;
        }

        if (zindex) {
            ElementStyleUtil.set(sub, 'z-index', zindex.toString());
        }

        if (width) {
            ElementStyleUtil.set(sub, 'width', width);
        }

        if (height) {
            ElementStyleUtil.set(sub, 'height', height);
        }

        this.initDropdownPopper(item, sub);

        item.classList.add('show');
        item.classList.add('menu-dropdown');
        sub.classList.add('show');

        // Append the sub the the root of the menu
        if (this._getItemOption(item, 'strategy') === 'absolute') {
            document.body.appendChild(sub);
            DataUtil.set(item, 'sub', sub);
            DataUtil.set(sub, 'item', item);
            DataUtil.set(sub, 'menu', this);
        } else {
            DataUtil.set(sub, 'item', item);
        }
        DataUtil.set(item, 'menu', this);

        EventHandlerUtil.trigger(this._element, 'shown.lt.menu');
    }

    // Init dropdown popper(new)
    private initDropdownPopper(item: HTMLElement, sub: HTMLElement) {
        // Setup popper instance
        let reference;
        const attach = this._getItemOption(item, 'attach') as string;

        if (attach) {
            if (attach === 'parent') {
                reference = item.parentNode;
            } else {
                reference = document.querySelector(attach);
            }
        } else {
            reference = item;
        }

        if (reference) {
            const popper = createPopper(
                reference as Element | VirtualElement,
                sub,
                this._getDropdownPopperConfig(item)
            );
            DataUtil.set(item, 'popper', popper);
        }
    }

    // Hide item dropdown
    private _hideDropdown = (item: HTMLElement) => {
        const menu = DataUtil.get(item, 'menu') as MenuWrapper;
        if (EventHandlerUtil.trigger(menu?.element, 'hide.lt.menu') === false) {
            return;
        }

        const sub = this.getItemSubElement(item);
        if (!sub) {
            return;
        }
        ElementStyleUtil.set(sub, 'z-index', '');
        ElementStyleUtil.set(sub, 'width', '');
        ElementStyleUtil.set(sub, 'height', '');
        item.classList.remove('show');
        item.classList.remove('menu-dropdown');
        sub.classList.remove('show');

        // Append the sub back to it's parent
        if (this._getItemOption(item, 'strategy') === 'absolute') {
            if (item.classList.contains('menu-item')) {
                item.appendChild(sub);
            } else {
                insertAfterElement(menu.element, item);
            }

            DataUtil.remove(item, 'sub');
            DataUtil.remove(sub, 'item');
            DataUtil.remove(sub, 'menu');
        }
        const popper = DataUtil.get(item as HTMLElement, 'popper') as Instance;
        if (popper) {
            popper.destroy();
            DataUtil.remove(item, 'popper');
        }

        // Destroy popper(new)
        this.destroyDropdownPopper(item);
        EventHandlerUtil.trigger(this._element, 'hidden.lt.menu');
    };

    // Destroy dropdown popper(new)
    private destroyDropdownPopper(item: HTMLElement) {
        const popper = DataUtil.get(item as HTMLElement, 'popper') as Instance;
        if (popper) {
            popper.destroy();
            DataUtil.remove(item, 'popper');
        }

        EventHandlerUtil.trigger(this._element, 'hidden.lt.menu');
    }

    private _showAccordion = (item: HTMLElement) => {
        if (EventHandlerUtil.trigger(this._element, 'show.lt.menu') === false) {
            return;
        }

        if (this.options.accordion.expand === false) {
            this.hideAccordions(item);
        }

        if (DataUtil.has(item, 'popper') === true) {
            this._hideDropdown(item);
        }

        const subElement = this.getItemSubElement(item);
        if (subElement) {
            item.classList.add('hover'); // updateWW
            item.classList.add('showing');
            const sub = subElement as HTMLElement;
            slideDown(sub, this.options.accordion.slideSpeed, () => {
                item.classList.remove('showing');
                item.classList.add('show');
                sub.classList.add('show');
                EventHandlerUtil.trigger(this._element, 'shown.lt.menu');
            });
        }
    };

    private _hideAccordion = (item: HTMLElement) => {
        if (EventHandlerUtil.trigger(this._element, 'hide.lt.menu') === false) {
            return;
        }

        const sub = this.getItemSubElement(item);
        item.classList.add('hiding');

        if (sub) {
            slideUp(sub, this.options.accordion.slideSpeed, () => {
                item.classList.remove('hiding');
                item.classList.remove('show');
                sub.classList.remove('show');
                item.classList.remove('hover'); // update
                EventHandlerUtil.trigger(this._element, 'hidden.lt.menu');
            });
        }
    };

    // Hide all shown accordions of item
    public hideAccordions(item: HTMLElement) {
        const itemsToHide = this._element.querySelectorAll('.show[data-lt-menu-trigger]');
        if (itemsToHide && itemsToHide.length > 0) {
            for (let i = 0, len = itemsToHide.length; i < len; i++) {
                const itemToHide = itemsToHide[i] as HTMLElement;

                if (
                    this.getItemSubType(itemToHide) === 'accordion' &&
                    itemToHide !== item &&
                    item.contains(itemToHide) === false &&
                    itemToHide.contains(item) === false
                ) {
                    this._hideAccordion(itemToHide);
                }
            }
        }
    }

    // Event Handlers
    // Reset item state classes if item sub type changed
    public reset(item: HTMLElement) {
        if (this._hasItemSub(item) === false) {
            return;
        }

        const sub = this.getItemSubElement(item);

        // Reset sub state if sub type is changed during the window resize
        if (DataUtil.has(item, 'type') && DataUtil.get(item, 'type') !== this.getItemSubType(item)) {
            // updated
            item.classList.remove('hover');
            item.classList.remove('show');
            item.classList.remove('show');
            if (sub) {
                sub.classList.remove('show');
            }
        } // updated
    }

    // TODO: not done
    private _destroy() {
        // noop
    }

    // Update all item state classes if item sub type changed
    public update() {
        const items = this._element.querySelectorAll('.menu-item[data-lt-menu-trigger]');
        items.forEach((el) => this.reset(el as HTMLElement));
    }

    // Hide item sub
    public hide(item: HTMLElement) {
        if (!item) {
            return;
        }

        if (this.isItemSubShown(item) === false) {
            return;
        }

        if (this.getItemSubType(item) === 'dropdown') {
            this._hideDropdown(item);
        } else if (this.getItemSubType(item) === 'accordion') {
            this._hideAccordion(item);
        }
    }

    // Show item sub
    public show(item: HTMLElement) {
        if (!item) {
            return;
        }

        if (this.isItemSubShown(item) === true) {
            return;
        }

        if (this.getItemSubType(item) === 'dropdown') {
            this._showDropdown(item); // // show current dropdown
        } else if (this.getItemSubType(item) === 'accordion') {
            this._showAccordion(item);
        }

        // Remember last submenu type

        DataUtil.set(item, 'type', this.getItemSubType(item)); // updated
    }

    // Toggle item sub
    private _toggle(item: HTMLElement) {
        if (!item) {
            return;
        }

        if (this.isItemSubShown(item) === true) {
            this.hide(item);
        } else {
            this.show(item);
        }
    }

    // Mouseout handle
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    public mouseout(element: HTMLElement, e: MouseEvent) {
        const item = this._getItemElement(element) as HTMLElement;
        if (!item) {
            return;
        }

        if (this._getItemOption(item, 'trigger') !== 'hover') {
            return;
        }

        const timeout = setTimeout(() => {
            if (DataUtil.get(item, 'hover') === '1') {
                this.hide(item);
            }
        }, this.options.dropdown.hoverTimeout);

        DataUtil.set(item, 'hover', '1');
        DataUtil.set(item, 'timeout', timeout);
    }

    // Mouseover handle
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    public mouseover(element: HTMLElement, e: MouseEvent) {
        const item = this._getItemElement(element) as HTMLElement;
        if (!item) {
            return;
        }

        if (this._getItemOption(item, 'trigger') !== 'hover') {
            return;
        }

        if (DataUtil.get(item, 'hover') === '1') {
            clearTimeout(DataUtil.get(item, 'timeout') as number);
            DataUtil.remove(item, 'hover');
            DataUtil.remove(item, 'timeout');
        }

        this.show(item);
    }

    // Dismiss handler
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    public dismiss(element: HTMLElement, e: Event) {
        const item = this._getItemElement(element) as HTMLElement;
        const items = this._getItemChildElements(item);
        //if ( item !== null && _getItemOption(item, 'trigger') === 'click' &&  _getItemSubType(item) === 'dropdown' ) {
        const itemSubType = this.getItemSubType(item);
        if (item !== null && itemSubType === 'dropdown') {
            this.hide(item); // hide items dropdown

            // Hide all child elements as well
            if (items.length > 0) {
                for (let i = 0, len = items.length; i < len; i++) {
                    //if ( _getItemOption(item, 'trigger') === 'click' &&  _getItemSubType(item) === 'dropdown' ) {
                    if (items[i] !== null && this.getItemSubType(items[i] as HTMLElement) === 'dropdown') {
                        this.hide(items[i] as HTMLElement);
                    }
                }
            }
        }
    }

    // Link handler
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    public link(element: HTMLElement, e: Event) {
        if (EventHandlerUtil.trigger(element, 'click.lt.menu') === false) {
            return;
        }

        // Dismiss all shown dropdowns
        MenuWrapper.hideDropdowns(undefined);
        EventHandlerUtil.trigger(this._element, 'clicked.lt.menu');
    }

    public click(element: HTMLElement, e: Event) {
        e.preventDefault();
        const item = this._getItemElement(element) as HTMLElement;
        if (this._getItemOption(item, 'trigger') !== 'click') {
            return;
        }

        if (this._getItemOption(item, 'toggle') === false) {
            this.show(item);
        } else {
            this._toggle(item);
        }
    }

    // General Methods
    public getItemTriggerType(item: HTMLElement) {
        return this._getItemOption(item, 'trigger');
    }

    // Event API
    public on(name: string, handler: EventListener) {
        return EventHandlerUtil.on(this._element, name, handler);
    }

    public one(name: string, handler: EventListener) {
        return EventHandlerUtil.one(this._element, name, handler);
    }

    public off(name: string) {
        return EventHandlerUtil.off(this._element, name);
    }
}
