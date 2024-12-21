import { Directive, EventEmitter, HostBinding, Input, Output } from '@angular/core';
import { PortletBodyComponent } from './portlet-body.component';
import { PortletComponent } from './portlet.component';

@Directive({
    // eslint-disable-next-line @angular-eslint/directive-selector
    selector: 'portlet-tab, [portlet-tab]',
    exportAs: 'portletTab',
})
export class PortletTabDirective {
    @Input()
    public heading: string | undefined;

    @Input()
    public icon: string | undefined;

    @Input()
    public iconCLass = 'svg-icon-1';

    @HostBinding('attr.class')
    public get cssClass(): string {
        let css = 'tab-pane';
        if (this.active) {
            css += ' active';
        }
        return css;
    }

    @HostBinding('attr.id')
    @Input()
    public id: string | undefined;

    @Input()
    public disabled = false;

    /** tab active state toggle */
    @Input()
    public get active(): boolean {
        return this._active;
    }

    public set active(active: boolean) {
        if (this._active === active) {
            return;
        }
        if ((this.disabled && active) || !active) {
            if (this._active && !active) {
                this.deselectTab.emit(this);
                this._active = active;
            }

            return;
        }

        this._active = active;
        this.selectTab.emit(this);
        this.parent?.tabset?.forEach((tab: PortletTabDirective) => {
            if (tab !== this) {
                tab.active = false;
            }
        });
    }

    /** fired when tab became active, $event:Tab equals to selected instance of Tab component */
    @Output()
    public selectTab: EventEmitter<PortletTabDirective> = new EventEmitter();
    /** fired when tab became inactive, $event:Tab equals to deselected instance of Tab component */
    @Output()
    public deselectTab: EventEmitter<PortletTabDirective> = new EventEmitter();

    public parent: PortletBodyComponent | undefined;
    protected _active = false;

    constructor() {
        // noop
    }

    public get root(): PortletComponent | undefined {
        if (this.parent) {
            return this.parent.parent;
        }
        return undefined;
    }
}
