import {
    AfterViewInit,
    ChangeDetectionStrategy,
    Component,
    ElementRef,
    Input,
    OnChanges,
    SimpleChanges,
} from '@angular/core';
import { InlineSVGDirective } from './inline-svg.directive';
import { InlineSVGService } from './inline-svg.service';

@Component({
    // eslint-disable-next-line @angular-eslint/component-selector
    selector: 'inline-svg-container',
    template: '',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InlineSVGContainerComponent implements AfterViewInit, OnChanges {
    @Input() context!: InlineSVGDirective;
    @Input() content: HTMLElement | SVGElement | undefined;
    @Input() replaceContents = false;
    @Input() prepend = false;

    /** @internal */
    _el: ElementRef;

    constructor(private _inlineSVGService: InlineSVGService, el: ElementRef<HTMLElement>) {
        this._el = el;
    }

    ngAfterViewInit(): void {
        this._updateContent();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['content']) {
            this._updateContent();
        }
    }

    private _updateContent(): void {
        if (this.content)
            this._inlineSVGService.insertEl(
                this.context,
                this._el.nativeElement,
                this.content,
                this.replaceContents,
                this.prepend
            );
    }
}
