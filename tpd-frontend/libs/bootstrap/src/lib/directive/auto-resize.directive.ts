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
    PartialRecord,
    getAttributeValueByBreakpoint,
    getCSS,
    getViewPort,
    isVisibleElement,
} from '@devacfr/util';
import { Subscription, fromEvent } from 'rxjs';
import { throttleTime } from 'rxjs/operators';

export interface AutoResizeOptions {
    activate: boolean | PartialRecord<Breakpoint, boolean>;
    height?: string | PartialRecord<Breakpoint, string> | ((el: HTMLElement) => string);
    minHeight?: string;
    maxHeight?: string;
    dependencies?: string;
    wrappers?: string;
    offset?: string | PartialRecord<Breakpoint, string> | ((el: HTMLElement) => string);
}

export const DefaultAutoResizeOptions: AutoResizeOptions = {
    activate: true,
};

@Directive({
    selector: '[ltAutoResize]',
    exportAs: 'ltAutoResize',
})
export class AutoResizeDirective implements OnInit, DoCheck, OnDestroy {
    @HostBinding('id')
    @Input()
    public id: string | undefined;

    @Input()
    public set ltAutoResize(options: AutoResizeOptions | Partial<AutoResizeOptions> | undefined) {
        this._options = Object.assign({}, DefaultAutoResizeOptions, options);
    }

    @Output()
    public initialize = new EventEmitter<void>();

    public get element(): HTMLElement {
        return this._element.nativeElement;
    }

    private _initialize = false;
    private _options: AutoResizeOptions | Partial<AutoResizeOptions> | undefined;
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

    public get options(): AutoResizeOptions | Partial<AutoResizeOptions> | undefined {
        return this._options;
    }

    public update() {
        // Activate/deactivate
        if (getAttributeValueByBreakpoint(this._options?.activate) === true) {
            this.setupHeight();
            if (!this._initialize) {
                this.initialize.emit();
                this._initialize = true;
            }
        } else {
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

    private getHeightType(): keyof AutoResizeOptions {
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

        if (this.options?.minHeight) {
            const minHeight = this._options?.minHeight ? parseInt(this._options.minHeight) : -1;
            if (minHeight > 0 && height < minHeight) {
                height = minHeight;
            }
        }
        if (this.options?.maxHeight) {
            const maxHeight = this._options?.maxHeight ? parseInt(this._options.maxHeight) : -1;
            if (maxHeight > 0 && height > maxHeight) {
                height = maxHeight;
            }
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

    private resetHeight() {
        const heghtType = this.getHeightType();
        if (heghtType) {
            this._renderer.setStyle(this.element, heghtType, '');
        }
    }
}
