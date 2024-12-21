import { Directive, ElementRef, Input, OnChanges, OnDestroy, OnInit, Renderer2, SimpleChanges } from '@angular/core';
import { Breakpoint, getAttributeValueByBreakpoint, PartialRecord } from '@devacfr/util';
import { fromEvent, Subscription } from 'rxjs';
import { throttleTime } from 'rxjs/operators';

export interface SwapperOptions {
    mode?: 'prepend' | 'append';
    parent: string | PartialRecord<Breakpoint, string>;
}

export const DefaultSwapperOptions: SwapperOptions = {
    parent: '',
    mode: 'append',
};

@Directive({
    selector: '[ltSwapper]',
    exportAs: 'ltSwapper',
})
export class SwapperDirective implements OnInit, OnDestroy, OnChanges {
    private _options: SwapperOptions | Partial<SwapperOptions> | undefined;

    private _subscriptions = new Subscription();

    constructor(private _elment: ElementRef, private _renderer: Renderer2) {}

    @Input()
    public set ltSwapper(options: SwapperOptions | Partial<SwapperOptions> | undefined) {
        this._options = Object.assign({}, DefaultSwapperOptions, options);
    }

    ngOnInit(): void {
        this._subscriptions.add(
            fromEvent(window, 'resize')
                .pipe(throttleTime(50))
                .subscribe(() => this.update())
        );
    }
    ngOnDestroy(): void {
        this._subscriptions.unsubscribe();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.ltSwapper && changes.ltSwapper.currentValue !== changes.ltSwapper.previousValue) {
            this.update();
        }
    }

    public update() {
        const element = this._elment.nativeElement as HTMLElement;
        const parentSelector = getAttributeValueByBreakpoint<string>(this._options?.parent);

        const mode = this._options?.mode;
        const parentElement = parentSelector ? document.querySelector(parentSelector) : null;

        if (parentElement && element.parentNode !== parentElement) {
            if (mode === 'prepend') {
                parentElement.prepend(element);
            } else if (mode === 'append') {
                parentElement.append(element);
            }
        }
    }
}
