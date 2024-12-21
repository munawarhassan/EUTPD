import { ChangeDetectorRef, Component, HostBinding, Input } from '@angular/core';
import { ClassBuilder } from '@devacfr/util';
import { mSvgIcons } from '../inline-svg';
import { AccordionComponent } from './accordion.component';

@Component({
    selector: 'lt-accordion-item',
    templateUrl: './accordion-item.component.html',
})
export class AccordionItemComponent {
    @Input()
    public isOpen = false;

    @Input()
    public headerClass = '';

    @Input()
    public icon: string | undefined;

    @Input()
    public heading: string | undefined;

    @Input()
    public iconCLass = 'svg-icon-1';

    @Input()
    public toggleSVG = mSvgIcons.Duotone.arrows.right;

    @Input()
    public displayed = true;

    public collapsed = true;

    @Input()
    @HostBinding('class')
    public get class(): string {
        const builder = ClassBuilder.create('accordion-item');
        if (this._class) {
            builder.css(this._class);
        }
        return builder.toString();
    }

    public set class(value: string) {
        this._class = value;
    }

    public _parent: AccordionComponent | undefined;
    private _class: string | undefined;

    constructor(private _cd: ChangeDetectorRef) {}

    public get parent(): AccordionComponent | undefined {
        return this._parent;
    }

    public set parent(v: AccordionComponent | undefined) {
        this._parent = v;
        this._cd.markForCheck();
    }

    public get useSVG(): boolean {
        return this.icon != null && this.icon.endsWith('.svg');
    }

    public get parentId(): string | undefined {
        return this.parent?.closeOthers ? '#' + this.parent.id : undefined;
    }

    public handleCollapse(collapsed: boolean) {
        this.collapsed = collapsed;
        this._cd.detectChanges();
    }
}
