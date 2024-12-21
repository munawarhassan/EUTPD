import { AfterViewInit, Directive, ElementRef, Input, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { Tooltip } from 'bootstrap';

@Directive({ selector: '[ltTooltip]', exportAs: 'ltTooltip' })
export class TooltipDirective implements AfterViewInit, OnChanges, OnDestroy {
    @Input()
    public ltTooltip: string | undefined;

    @Input()
    public trigger:
        | 'click'
        | 'hover'
        | 'focus'
        | 'manual'
        | 'click hover'
        | 'click focus'
        | 'hover focus'
        | 'click hover focus' = 'hover';
    @Input()
    public placement: Tooltip.PopoverPlacement | (() => Tooltip.PopoverPlacement) = 'auto';

    @Input()
    public container: string | undefined;

    private _bsElement: Tooltip | undefined;

    constructor(private _element: ElementRef) {}

    ngOnChanges(changes: SimpleChanges): void {
        let toUpdate = false;
        if (changes.ltTooltip && changes.ltTooltip.currentValue != changes.ltTooltip.previousValue) {
            toUpdate = true;
        }
        if (toUpdate) {
            this.initToolTip();
        }
    }

    public ngAfterViewInit(): void {
        this.initToolTip();
    }

    public ngOnDestroy(): void {
        this._bsElement?.dispose();
    }

    private getOptions(): Partial<Tooltip.Options> {
        const container = this.container ? this.container : 'body';
        return {
            placement: this.placement,
            title: this.ltTooltip,
            container,
            trigger: this.trigger,
        };
    }

    private initToolTip(): void {
        if (!this.ltTooltip) {
            return;
        }
        if (this._bsElement) {
            this._bsElement.dispose();
        }
        this._bsElement = new Tooltip(this._element.nativeElement, this.getOptions());
    }
}
