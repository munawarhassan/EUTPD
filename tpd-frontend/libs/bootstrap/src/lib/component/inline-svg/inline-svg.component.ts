import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    Host,
    HostBinding,
    Input,
    OnChanges,
    SimpleChanges,
} from '@angular/core';
import { BreakpointValue, BsColor, ClassBuilder } from '@devacfr/util';
import { InlineSVGDirective } from './inline-svg.directive';

@Component({
    // eslint-disable-next-line @angular-eslint/component-selector
    selector: 'inline-svg',
    template: ``,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InlineSvgComponent implements OnChanges {
    @Input()
    public color: BsColor | string | undefined;

    @Input()
    public size: BreakpointValue | undefined;

    @Input()
    public get src(): string | undefined {
        return this._directive.inlineSVG;
    }

    public set src(value: string | undefined) {
        this._directive.inlineSVG = value;
    }

    constructor(@Host() private _directive: InlineSVGDirective, private _cd: ChangeDetectorRef) {}
    ngOnChanges(changes: SimpleChanges): void {
        if (changes.src && changes.src.currentValue !== changes.src.previousValue) {
            this._directive._insertSVG();
        }
    }

    @HostBinding('class')
    public get classes(): string {
        const builder = ClassBuilder.create('svg-icon');
        if (this.color) {
            builder.flag('svg-icon-', this.color);
        }
        if (this.size) builder.breakpoint('svg-icon-', this.size);
        return builder.toString();
    }
}
