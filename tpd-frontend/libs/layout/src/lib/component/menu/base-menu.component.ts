import { ElementRef, Injectable, Input, OnDestroy, Renderer2 } from '@angular/core';
import { EventHandlerUtil } from '@devacfr/util';
import { fromEvent, Subscription } from 'rxjs';
import { throttleTime } from 'rxjs/operators';
import { MenuType } from '.';
import { DefaultMenuOptions, MenuWrapper } from './menu-wrapper';

@Injectable()
export class BaseMenuComponent implements OnDestroy {
    @Input()
    public options = DefaultMenuOptions;

    @Input()
    public menuType: MenuType = 'dropdown';

    protected component: MenuWrapper | undefined;

    protected subscriptions = new Subscription();

    constructor(protected _element: ElementRef, protected _renderer: Renderer2) {}

    protected init(menuType: MenuType): void {
        const options = Object.assign({}, DefaultMenuOptions, this.options);
        this._renderer.addClass(this._element.nativeElement, 'menu');
        this.component = new MenuWrapper(this._element.nativeElement, menuType, options);
        if (this.component.triggerElement) {
            this.subscriptions.add(
                fromEvent(this.component.triggerElement, 'click').subscribe((ev) => {
                    if (this.component && this.component.triggerElement) {
                        this.component.click(this.component.triggerElement, ev);
                    }
                })
            );
        }

        this.subscriptions.add(
            fromEvent(window, 'resize')
                .pipe(
                    throttleTime(200) // emits once, then ignores subsequent emissions for 300ms, repeat...
                )
                .subscribe(() => this.component?.update())
        );
    }

    public ngOnDestroy(): void {
        this.subscriptions.unsubscribe();
    }

    public handleMouseOut(event: Event) {
        const element = this._element.nativeElement as HTMLElement;
        EventHandlerUtil.forEachQuerySelector(element, event, '[data-lt-menu-trigger], .menu-sub', (target, e) =>
            this.component?.mouseout(target, e as MouseEvent)
        );
    }

    public handleMouseOver(event: Event) {
        const element = this._element.nativeElement as HTMLElement;
        EventHandlerUtil.forEachQuerySelector(element, event, '[data-lt-menu-trigger], .menu-sub', (target, e) =>
            this.component?.mouseover(target, e as MouseEvent)
        );
    }

    public handleClick(event: Event) {
        const element = this._element.nativeElement as HTMLElement;
        // dismiss handler
        EventHandlerUtil.forEachQuerySelector(element, event, '[data-lt-menu-dismiss="true"]', (target, e) =>
            this.component?.dismiss(target, e as MouseEvent)
        );
        // link handler
        EventHandlerUtil.forEachQuerySelector(
            element,
            event,
            '.menu-item:not([data-lt-menu-trigger]) > .menu-link',
            (target, e) => {
                e.stopPropagation();
                this.component?.link(target, e);
            }
        );
        // toggle handler
        EventHandlerUtil.forEachQuerySelector(
            element,
            event,
            '.menu-item[data-lt-menu-trigger] > .menu-link, [data-lt-menu-trigger]:not(.menu-item):not([data-lt-menu-trigger="auto"])',
            (target, e) => this.component?.click(target, e)
        );
    }

    public handleDropdown(event: Event) {
        const menuItems = document.querySelectorAll('.show.menu-dropdown[data-lt-menu-trigger]');
        if (menuItems && menuItems.length > 0) {
            for (let i = 0; i < menuItems.length; i++) {
                const item = menuItems[i] as HTMLElement;
                const menuObj = this.component;
                if (menuObj && menuObj.getItemSubType(item) === 'dropdown') {
                    const sub = menuObj.getItemSubElement(item) as HTMLElement;
                    if (item === event.target || item.contains(event.target as HTMLElement)) {
                        continue;
                    }
                    if (sub && (sub === event.target || sub.contains(event.target as HTMLElement))) {
                        continue;
                    }
                    menuObj.hide(item);
                }
            }
        }
    }
}
