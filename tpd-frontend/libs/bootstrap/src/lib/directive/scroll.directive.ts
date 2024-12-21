import { isPlatformBrowser } from '@angular/common';
import {
    Directive,
    DoCheck,
    ElementRef,
    EventEmitter,
    HostBinding,
    Inject,
    Input,
    KeyValueDiffer,
    KeyValueDiffers,
    OnDestroy,
    OnInit,
    Output,
    PLATFORM_ID,
    Renderer2,
} from '@angular/core';
import {
    Breakpoint,
    ClassBuilder,
    getAttributeValueByBreakpoint,
    getCSS,
    getViewPort,
    isVisibleElement,
    PartialRecord,
} from '@devacfr/util';
import { fromEvent, Subscription } from 'rxjs';
import { throttleTime } from 'rxjs/operators';

export interface ScrollOptions {
    activate: boolean | PartialRecord<Breakpoint, boolean>;
    height?: string | PartialRecord<Breakpoint, string> | ((el: HTMLElement) => string);
    minHeight?: string;
    maxHeight?: string;
    dependencies?: string;
    wrappers?: string;
    offset?: string | PartialRecord<Breakpoint, string> | ((el: HTMLElement) => string);
    saveState?: boolean;
    suppressScroll?: 'x' | 'y';
}

export const DefaultScrollOptions: ScrollOptions = {
    activate: true,
    height: 'auto',
    saveState: true,
};

@Directive({
    selector: '[ltScroll]',
    exportAs: 'ltScroll',
})
export class ScrollDirective implements OnInit, DoCheck, OnDestroy {
    @HostBinding('id')
    @Input()
    public id: string | undefined;

    @Input()
    @HostBinding('class')
    public set class(value: string) {
        this._class = value;
    }

    public get class(): string {
        const builder = ClassBuilder.create(this._class);
        return builder.toString();
    }

    @Input()
    public set ltScroll(options: ScrollOptions | Partial<ScrollOptions> | undefined) {
        this._options = Object.assign({}, DefaultScrollOptions, options);
    }

    @Output()
    public initialize = new EventEmitter<void>();

    @Output()
    public reachYEnd = new EventEmitter<ScrollDirective>();

    public get element(): HTMLElement {
        return this._element.nativeElement;
    }

    private _class = 'hover-scroll scroll-me';
    private _initialize = false;
    private _options: ScrollOptions | Partial<ScrollOptions> | undefined;
    private _optionsDiff: KeyValueDiffer<string, unknown> | undefined;

    private _subscriptions = new Subscription();

    constructor(
        private _element: ElementRef<HTMLElement>,
        private _renderer: Renderer2,
        private differs: KeyValueDiffers,
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        @Inject(PLATFORM_ID) private platformId: any
    ) {}

    ngOnInit(): void {
        if (!this._optionsDiff) {
            this._optionsDiff = this.differs.find(this._options || {}).create();
            this._optionsDiff.diff(this._options || {});
        }

        this._subscriptions.add(
            fromEvent(window, 'resize')
                .pipe(throttleTime(200))
                .subscribe(() => this.update())
        );
        this._subscriptions.add(fromEvent(this.element, 'scroll').subscribe(() => this.handlerScroll()));
        setTimeout(() => this.update(), 500);
    }

    ngOnDestroy(): void {
        this._subscriptions.unsubscribe();
    }

    ngDoCheck(): void {
        if (this._options?.activate && this._optionsDiff && isPlatformBrowser(this.platformId)) {
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            const changes = this._optionsDiff?.diff((this._options as any) || {});
            if (changes) {
                this.update();
            }
        }
    }

    public get options(): ScrollOptions | Partial<ScrollOptions> | undefined {
        return this._options;
    }

    public set scrollTop(value: number) {
        this.element.scrollTop = value;
    }

    public get scrollTop(): number {
        return this.element.scrollTop;
    }

    public get hasHorizontalScrollbar(): boolean {
        return this.element.scrollWidth > this.element.clientWidth;
    }

    public get hasVerticalScrollbar(): boolean {
        return this.element.scrollHeight > this.element.clientHeight;
    }

    // @HostListener('document:click')
    // handlerClick() {
    // if (this._options?.saveState === true && this.id) {
    //     const cookieId = this.id + 'st';
    //     CookieComponent.set(cookieId, this.element.scrollTop, {});
    // }
    // }

    handlerScroll() {
        const contentEl = this.element.firstChild as HTMLElement;
        if (contentEl) {
            const rect = contentEl.getBoundingClientRect();
            const height = Math.round(rect.height) - this.element.clientHeight - 30;
            // console.log('BEFORE: scrollTop:' + this.element.scrollTop + ',reachYEnd:' + height);
            if (this.element.scrollTop >= height) {
                // console.log('EMIT: scrollTop:' + this.element.scrollTop + ',reachYEnd:' + height);
                this.reachYEnd.emit(this);
            }
        }
    }

    public update() {
        // Activate/deactivate
        if (getAttributeValueByBreakpoint(this._options?.activate) === true) {
            if (this._options?.suppressScroll === 'x') {
                this._renderer.setStyle(this.element, 'overflow-x', 'hidden');
            } else if (this._options?.suppressScroll === 'y') {
                this._renderer.setStyle(this.element, 'overflow-y', 'hidden');
            } else {
                this._renderer.setStyle(this.element, 'overflow-x', '');
                this._renderer.setStyle(this.element, 'overflow-y', '');
            }
            this.setupHeight();
            this.setupState();
            if (!this._initialize) {
                this.initialize.emit();
                this._initialize = true;
            }
        } else {
            this._renderer.setStyle(this.element, 'overflow-x', '');
            this._renderer.setStyle(this.element, 'overflow-y', '');
            this.resetHeight();
        }
    }

    public getHeight(): string | undefined {
        if (!this._options) return '';
        const heightType = this.getHeightType();
        const height = this._options[heightType];
        if (typeof height === 'object') {
            return getAttributeValueByBreakpoint<string>(height as PartialRecord<Breakpoint, string>);
        } else if (typeof height === 'function') {
            return height(this.element);
        } else if (height !== null && typeof height === 'string' && height.toLowerCase() === 'auto') {
            return this.getAutoHeight();
        } else {
            return height as string;
        }
    }

    private getHeightType(): keyof ScrollOptions {
        if (this._options?.height) {
            return 'height';
        }
        if (this._options?.minHeight) {
            return 'minHeight';
        }
        if (this._options?.maxHeight) {
            return 'maxHeight';
        }
        return 'height';
    }

    private getAutoHeight(): string {
        let height = getViewPort().height;
        const dependencies = this._options?.dependencies;
        const wrappers = this._options?.wrappers;
        let offset;
        if (typeof this._options?.offset === 'function') {
            offset = this._options.offset(this.element);
        } else {
            offset = getAttributeValueByBreakpoint(this._options?.offset);
        }

        // minus top of element and viewport height
        const rect = this.element.getBoundingClientRect();
        height -= rect.top - 30;

        // Height dependencies
        if (dependencies !== null) {
            const elements = document.querySelectorAll(dependencies as string);
            if (elements && elements.length > 0) {
                for (let i = 0, len = elements.length; i < len; i++) {
                    const element = elements[i] as HTMLElement;
                    if (isVisibleElement(element) === false) {
                        continue;
                    }

                    height = height - parseInt(getCSS(element, 'height'));
                    height = height - parseInt(getCSS(element, 'margin-top'));
                    height = height - parseInt(getCSS(element, 'margin-bottom'));

                    const borderTop = getCSS(element, 'border-top');
                    if (borderTop) {
                        height = height - parseInt(borderTop);
                    }

                    const borderBottom = getCSS(element, 'border-bottom');
                    if (borderBottom) {
                        height = height - parseInt(borderBottom);
                    }
                }
            }
        }

        // Wrappers
        if (wrappers != null) {
            const elements = document.querySelectorAll(wrappers);
            if (elements && elements.length > 0) {
                for (let i = 0, len = elements.length; i < len; i++) {
                    const element = elements[i] as HTMLElement;

                    if (!isVisibleElement(element)) {
                        continue;
                    }

                    height = height - parseInt(getCSS(element, 'margin-top'));
                    height = height - parseInt(getCSS(element, 'margin-bottom'));
                    height = height - parseInt(getCSS(element, 'padding-top'));
                    height = height - parseInt(getCSS(element, 'padding-bottom'));

                    const borderTop = getCSS(element, 'border-top');
                    if (borderTop) {
                        height = height - parseInt(borderTop);
                    }

                    const borderBottom = getCSS(element, 'border-bottom');
                    if (borderBottom) {
                        height = height - parseInt(borderBottom);
                    }
                }
            }
        }

        // Custom offset
        if (offset != null && typeof offset !== 'object') {
            height = height - parseInt(offset as string);
        }
        height = height - parseInt(getCSS(this.element, 'margin-top'));
        height = height - parseInt(getCSS(this.element, 'margin-bottom'));

        const borderTop = getCSS(this.element, 'border-top');
        if (borderTop) {
            height = height - parseInt(borderTop);
        }

        const borderBottom = getCSS(this.element, 'border-bottom');
        if (borderBottom) {
            height = height - parseInt(borderBottom);
        }

        return String(height) + 'px';
    }

    private setupHeight() {
        const height = this.getHeight();
        const heightType = this.getHeightType();

        // Set height
        if (height != null && height.length > 0) {
            this._renderer.setStyle(this.element, heightType, height);
        } else {
            this._renderer.setStyle(this.element, heightType, '');
        }
    }

    private setupState() {
        // if (this._options?.saveState === true && this.id) {
        //     const cookie = CookieComponent.get(this.id + 'st');
        //     if (cookie) {
        //         const pos = parseInt(cookie);
        //         if (pos > 0) {
        //             this.element.scrollTop = pos;
        //         }
        //     }
        // }
    }

    private resetHeight() {
        const heghtType = this.getHeightType();
        if (heghtType) {
            this._renderer.setStyle(this.element, heghtType, '');
        }
    }
}
