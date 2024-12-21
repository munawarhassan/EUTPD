import { Directive, ElementRef, HostListener, Input, OnChanges, Renderer2, SimpleChanges } from '@angular/core';
import {
    Breakpoint,
    ElementAnimateUtil,
    EventHandlerUtil,
    getAttributeValueByBreakpoint,
    getCSS,
    getElementOffset,
    getObjectPropertyValueByKey,
    getScrollTop,
    PartialRecord,
} from '@devacfr/util';

export interface StickyOptions {
    name?: string;
    offset?: string | boolean | PartialRecord<Breakpoint, string | boolean>;
    reverse?: boolean;
    width: string | PartialRecord<Breakpoint, string>;
    left?: string | PartialRecord<Breakpoint, string>;
    top?: string | PartialRecord<Breakpoint, string>;
    zindex?: number;
    animation: boolean;
    animationSpeed: string;
    animationClass: string;
}

export const DefaultStickyOptions: StickyOptions = {
    width: '',
    offset: '200',
    reverse: false,
    animation: true,
    animationSpeed: '0.3s',
    animationClass: 'animation-slide-in-down',
};

@Directive({
    selector: '[ltStickyful]',
    exportAs: 'ltStickyful',
})
export class StickyfulDirective implements OnChanges {
    private _options: StickyOptions | Partial<StickyOptions> | undefined;

    private eventTriggerState: boolean;
    private lastScrollTop: number;

    constructor(private _element: ElementRef, private _renderer: Renderer2) {
        this.eventTriggerState = true;
        this.lastScrollTop = 0;
    }
    ngOnChanges(changes: SimpleChanges): void {
        if (changes.ltStickyful && changes.ltStickyful.currentValue !== changes.ltStickyful.previousValue) {
            this.scroll();
        }
    }

    @Input()
    public set ltStickyful(options: StickyOptions | Partial<StickyOptions> | undefined) {
        this._options = Object.assign({}, DefaultStickyOptions, options);
    }
    public get element(): HTMLElement {
        return this._element.nativeElement;
    }

    public get instanceName(): string | undefined {
        return this._options?.name;
    }

    public get attributeName(): string {
        return 'data-lt-sticky-' + this.instanceName;
    }

    @HostListener('window:scroll')
    public scroll() {
        const offset = getAttributeValueByBreakpoint(this._options?.offset);
        const reverse = this._options?.reverse;

        // Exit if false
        if (offset === false) {
            return;
        }

        let offsetNum = 0;
        if (typeof offset === 'string') {
            offsetNum = parseInt(offset);
        }

        const st = getScrollTop();

        // Reverse scroll mode
        if (reverse === true) {
            // Release on reverse scroll mode
            if (st > offsetNum && this.lastScrollTop < st) {
                if (document.body.hasAttribute(this.attributeName) === false) {
                    this.enable();
                    document.body.setAttribute(this.attributeName, 'on');
                }

                if (this.eventTriggerState === true) {
                    EventHandlerUtil.trigger(this.element, 'kt.sticky.on');
                    EventHandlerUtil.trigger(this.element, 'kt.sticky.change');

                    this.eventTriggerState = false;
                }
            } else {
                // Back scroll mode
                if (document.body.hasAttribute(this.attributeName)) {
                    this.disable();
                    document.body.removeAttribute(this.attributeName);
                }

                if (this.eventTriggerState === false) {
                    EventHandlerUtil.trigger(this.element, 'kt.sticky.off');
                    EventHandlerUtil.trigger(this.element, 'kt.sticky.change');

                    this.eventTriggerState = true;
                }
            }

            this.lastScrollTop = st;
            return;
        }

        // Classic scroll mode
        if (st > offsetNum) {
            if (document.body.hasAttribute(this.attributeName) === false) {
                this.enable();
                document.body.setAttribute(this.attributeName, 'on');
            }

            if (this.eventTriggerState === true) {
                EventHandlerUtil.trigger(this.element, 'kt.sticky.on');
                EventHandlerUtil.trigger(this.element, 'kt.sticky.change');
                this.eventTriggerState = false;
            }
        } else {
            // back scroll mode
            if (document.body.hasAttribute(this.attributeName) === true) {
                this.disable();
                document.body.removeAttribute(this.attributeName);
            }

            if (this.eventTriggerState === false) {
                EventHandlerUtil.trigger(this.element, 'kt.sticky.off');
                EventHandlerUtil.trigger(this.element, 'kt.sticky.change');
                this.eventTriggerState = true;
            }
        }
    }

    private disable() {
        this._renderer.removeStyle(this.element, 'top');
        this._renderer.removeStyle(this.element, 'width');
        this._renderer.removeStyle(this.element, 'left');
        this._renderer.removeStyle(this.element, 'right');
        this._renderer.removeStyle(this.element, 'z-index');
        this._renderer.removeStyle(this.element, 'position');
    }

    private enable(update: boolean = false) {
        const top = getAttributeValueByBreakpoint(this._options?.top);
        const left = getAttributeValueByBreakpoint(this._options?.left);
        // const right = this.getOption("right");
        let width = getAttributeValueByBreakpoint(this._options?.width);
        const zindex = getAttributeValueByBreakpoint(this._options?.zindex);

        if (update !== true && this._options?.animationSpeed && this._options?.animation === true) {
            this._renderer.setStyle(this.element, 'animationDuration', this._options?.animationSpeed);
            ElementAnimateUtil.animateClass(this.element, 'animation ' + this._options?.animationClass);
        }

        if (zindex !== null) {
            this._renderer.setStyle(this.element, 'z-index', zindex);
            this._renderer.setStyle(this.element, 'position', 'fixed');
        }

        if (top !== null) {
            this._renderer.setStyle(this.element, 'top', top);
        }

        if (width !== null && width !== undefined) {
            const widthTarget = getObjectPropertyValueByKey(width, 'target') as string;
            if (widthTarget) {
                const targetElement = document.querySelector(widthTarget) as HTMLElement;
                if (targetElement) {
                    width = getCSS(targetElement, 'width');
                }
            }
            this._renderer.setStyle(this.element, 'width', width);
        }

        if (left !== null) {
            if (String(left).toLowerCase() === 'auto') {
                const offsetLeft = getElementOffset(this.element).left;

                if (offsetLeft > 0) {
                    this._renderer.setStyle(this.element, 'left', String(offsetLeft) + 'px');
                }
            }
        }
    }
}
