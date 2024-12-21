import { ChangeDetectionStrategy, Component, Host, HostBinding, Input, OnChanges, SimpleChanges } from '@angular/core';
import { BreakpointValue, ClassBuilder } from '@devacfr/util';
import { SvgIcons } from './svg-icons';
import { InlineSVGDirective } from './inline-svg.directive';

@Component({
    // eslint-disable-next-line @angular-eslint/component-selector
    selector: 'inline-svg-file',
    template: ``,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InlineSvgFileComponent implements OnChanges {
    @Input()
    public size: BreakpointValue | undefined;

    @Input()
    public get extension(): string | undefined {
        return this._extension;
    }

    public set extension(value: string | undefined) {
        this._extension = value;
        if (value) {
            this._directive.inlineSVG = this.svgIcons.Files[value];
        } else {
            this._directive.inlineSVG = undefined;
        }
    }

    private _extension: string | undefined;

    constructor(private svgIcons: SvgIcons, @Host() private _directive: InlineSVGDirective) {}
    ngOnChanges(changes: SimpleChanges): void {
        if (changes.src && changes.src.currentValue !== changes.src.previousValue) {
            this._directive._insertSVG();
        }
    }

    @HostBinding('class')
    public get classes(): string {
        const builder = ClassBuilder.create('svg-original');
        if (this.size) builder.breakpoint('svg-icon-', this.size);
        return builder.toString();
    }
}
