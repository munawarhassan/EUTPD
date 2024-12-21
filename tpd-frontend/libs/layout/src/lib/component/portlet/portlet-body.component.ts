import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    ContentChildren, //
    ElementRef,
    HostBinding,
    Input,
    QueryList,
    ViewEncapsulation,
} from '@angular/core';
import { PortletBodyModeType } from './typing';
import { ClassBuilder } from '@devacfr/util';
import { PortletTabDirective } from './portlet-tab.directive';
import { PortletComponent } from './portlet.component';

@Component({
    selector: 'lt-portlet-body',
    templateUrl: './portlet-body.component.html',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PortletBodyComponent {
    @Input()
    public mode: PortletBodyModeType[] | PortletBodyModeType | undefined;

    @ContentChildren(PortletTabDirective)
    public tabset: QueryList<PortletTabDirective> | undefined;

    @HostBinding('class')
    @Input()
    public get class(): string {
        const styleBuilder = ClassBuilder.create('portlet-body');
        if (this._cssClass) styleBuilder.css(this._cssClass);
        if (this.mode) {
            styleBuilder.flag('portlet-body-', ...this.mode);
        }
        return styleBuilder.toString();
    }

    public set class(value: string) {
        this._cssClass = value;
    }

    public _parent: PortletComponent | undefined;

    private _cssClass: string | undefined;

    constructor(private _cd: ChangeDetectorRef, public elementRef: ElementRef) {
        // noop
    }

    public set parent(parent: PortletComponent) {
        this._parent = parent;
        if (this.tabset) {
            this.tabset.forEach((tab, index) => {
                tab.parent = this;
                tab.active = index === 0;
            });
        }
        this._cd.markForCheck();
    }
}
