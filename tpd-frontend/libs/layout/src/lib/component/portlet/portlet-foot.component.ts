import { ChangeDetectionStrategy, Component, ElementRef, HostBinding, Input, ViewEncapsulation } from '@angular/core';
import { PortletFooterModeType } from './typing';
import { ClassBuilder } from '@devacfr/util';

@Component({
    selector: 'lt-portlet-foot',
    template: '<ng-content></ng-content>',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PortletFootComponent {
    @Input()
    public mode: PortletFooterModeType[] | PortletFooterModeType | undefined;

    private cachedClasses: string | undefined;

    @HostBinding('attr.class')
    @Input()
    public get class(): string {
        if (this.cachedClasses) {
            return this.cachedClasses;
        }
        const styleBuilder = ClassBuilder.create('portlet-foot');
        if (this._cssClass) styleBuilder.css(this._cssClass);
        if (this.mode) {
            styleBuilder.flag('portlet-foot--', ...this.mode);
        }
        this.cachedClasses = styleBuilder.toString();
        return this.cachedClasses;
    }

    public set class(value: string) {
        this._cssClass = value;
    }

    private _cssClass: string | undefined;

    constructor(public elementRef: ElementRef) {
        // noop
    }
}
