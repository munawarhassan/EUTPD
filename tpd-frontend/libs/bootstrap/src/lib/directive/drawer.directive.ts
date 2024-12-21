import {
    Directive,
    ElementRef,
    HostBinding,
    HostListener,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    Renderer2,
    RendererStyleFlags2,
    SimpleChanges,
} from '@angular/core';
import { NavigationCancel, NavigationEnd, Router } from '@angular/router';
import {
    Breakpoint,
    ElementStyleUtil,
    EventHandlerUtil,
    getAttributeValueByBreakpoint,
    getCSS,
    PartialRecord,
} from '@devacfr/util';
import { fromEvent, Subscription } from 'rxjs';
import { throttleTime } from 'rxjs/operators';

export interface DrawerOptions {
    overlay: boolean;
    baseClass: string;
    overlayClass: string;
    direction: 'start' | 'end';
    //extend
    name: string;
    enable: boolean;
    activate?: boolean | PartialRecord<Breakpoint, boolean>;
    width?: string | PartialRecord<Breakpoint, string>;
    toggle?: string;
    close?: string;
}

export const DefaultDrawerOptions: DrawerOptions = {
    name: 'adrawer',
    enable: true,
    activate: true,
    overlay: true,
    baseClass: 'drawer',
    overlayClass: 'drawer-overlay',
    direction: 'end',
};

@Directive({
    selector: '[ltDrawer]',
    exportAs: 'ltDrawer',
})
export class DrawerDirective implements OnInit, OnChanges, OnDestroy {
    @HostBinding('attr.data-lt-drawer')
    public get enable(): boolean | undefined {
        return this._options?.enable;
    }

    public shown = false;

    private toggleElement: HTMLElement | null = null;
    private overlayElement: HTMLElement | null = null;

    private _options: DrawerOptions | Partial<DrawerOptions> | undefined;

    private _subscriptions = new Subscription();

    constructor(private _element: ElementRef<HTMLElement>, private _router: Router, private _renderer: Renderer2) {
        this._subscriptions.add(
            this._router.events.subscribe((event) => {
                if (event instanceof NavigationEnd || event instanceof NavigationCancel) {
                    this.hide();
                }
            })
        );
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.ltDrawer && changes.ltDrawer.currentValue !== changes.ltDrawer.previousValue) {
            this.update();
        }
    }

    @Input()
    public set ltDrawer(options: DrawerOptions | Partial<DrawerOptions> | undefined) {
        this._options = Object.assign({}, DefaultDrawerOptions, options);
    }

    ngOnInit(): void {
        this._subscriptions.add(
            fromEvent(window, 'resize')
                .pipe(throttleTime(50))
                .subscribe(() => this.update())
        );
    }

    ngOnDestroy(): void {
        this.hide();
        this._subscriptions.unsubscribe();
    }

    @HostListener('document:click', ['$event'])
    public handleClick(event: Event) {
        const togglers = this._options?.toggle;
        const closers = this._options?.close;

        if (togglers) {
            EventHandlerUtil.forEachQuerySelector(document.body, event, togglers, (target, e) => {
                e.preventDefault();
                this.toggleElement = target;
                this.toggle();
            });
        }
        if (closers) {
            EventHandlerUtil.forEachQuerySelector(document.body, event, closers, (target, e) => {
                e.preventDefault();
                this.hide();
            });
        }
    }

    public hide(): void {
        const element = this._element.nativeElement;
        if (EventHandlerUtil.trigger(element, 'lt.drawer.hide') === false) {
            return;
        }

        this.shown = false;
        this.deleteOverlay();
        this._renderer.removeAttribute(document.body, `data-lt-drawer-${this._options?.name}`);
        this._renderer.removeAttribute(document.body, `data-lt-drawer`);
        this._renderer.removeClass(element, `${this._options?.baseClass}-on`);
        if (this.toggleElement != null) {
            this._renderer.removeClass(this.toggleElement, 'active');
        }

        EventHandlerUtil.trigger(element, 'lt.drawer.after.hidden');
    }

    public show() {
        this.update();
        const element = this._element.nativeElement;
        if (EventHandlerUtil.trigger(element, 'lt.drawer.show') === false) {
            return;
        }

        this.shown = true;
        this.createOverlay();
        this._renderer.setAttribute(document.body, `data-lt-drawer-${this._options?.name}`, 'on');
        this._renderer.setAttribute(document.body, 'data-lt-drawer', 'on');
        this._renderer.addClass(element, `${this._options?.baseClass}-on`);
        if (this.toggleElement !== null) {
            this._renderer.addClass(this.toggleElement, 'active');
        }

        EventHandlerUtil.trigger(element, 'lt.drawer.shown');
    }

    public update(): void {
        const element = this._element.nativeElement as HTMLElement;
        const width = getAttributeValueByBreakpoint(this._options?.width) as string;
        const direction = this._options?.direction;

        // Reset state
        const hasBaseClass = element.classList.contains(`${this._options?.baseClass}-on`);
        const bodyCanvasAttr = String(document.body.getAttribute(`data-lt-drawer-${this._options?.name}`));

        if (hasBaseClass === true && bodyCanvasAttr === 'on') {
            this.shown = true;
        } else {
            this.shown = false;
        }

        // Activate/deactivate
        if (getAttributeValueByBreakpoint<boolean>(this._options?.activate) === true) {
            if (this._options?.baseClass) {
                this._renderer.addClass(element, this._options.baseClass);
                this._renderer.addClass(element, `${this._options.baseClass}-${direction}`);
            }
            this._renderer.setStyle(element, 'width', width, RendererStyleFlags2.Important);
        } else {
            this._renderer.setStyle(element, 'width', '');
            if (this._options?.baseClass) {
                this._renderer.removeClass(element, this._options.baseClass);
                this._renderer.removeClass(element, `${this._options.baseClass}-${direction}`);
            }
            this.hide();
        }
    }

    public toggle(): void {
        const element = this._element.nativeElement;
        if (EventHandlerUtil.trigger(element, 'lt.drawer.toggle') === false) {
            return;
        }

        if (this.shown) {
            this.hide();
        } else {
            this.show();
        }

        EventHandlerUtil.trigger(element, 'lt.drawer.toggled');
    }

    private createOverlay() {
        const element = this._element.nativeElement;
        if (this._options?.overlay) {
            this.overlayElement = document.createElement('DIV');
            const elementZIndex = getCSS(element, 'z-index');
            if (elementZIndex) {
                const overlayZindex = parseInt(elementZIndex) - 1;
                ElementStyleUtil.set(this.overlayElement, 'z-index', overlayZindex.toString()); // update
            }
            document.body.append(this.overlayElement);
            const overlayClassOption = this._options.overlayClass;
            if (overlayClassOption) {
                this.overlayElement.classList.add(overlayClassOption);
            }
            this.overlayElement.addEventListener('click', (e) => {
                e.preventDefault();
                this.hide();
            });
        }
    }

    private deleteOverlay() {
        if (this.overlayElement !== null && this.overlayElement.parentNode) {
            this.overlayElement.parentNode.removeChild(this.overlayElement);
        }
    }
}
