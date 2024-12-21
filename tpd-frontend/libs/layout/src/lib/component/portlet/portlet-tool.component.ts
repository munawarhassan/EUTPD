import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { PortletToolType } from './typing';
import { PortletHeadComponent } from './portlet-head.component';
import { PortletComponent } from './portlet.component';
import { ClassBuilder } from '@devacfr/util';
import { mSvgIcons } from '@devacfr/bootstrap';

interface IconConfig extends Record<PortletToolType, { icon: string } | { on: string; off: string }> {
    toggle: { icon: string };
    fullscreen: { on: string; off: string };
    reload: { icon: string };
    remove: { icon: string };
}

const config: IconConfig = {
    toggle: {
        icon: mSvgIcons.Duotone.arrows.down,
    },
    fullscreen: {
        on: mSvgIcons.Simple.design.position,
        off: mSvgIcons.Duotone.abstract.abs010,
    },
    reload: {
        icon: mSvgIcons.Duotone.arrows.circleAround,
    },
    remove: {
        icon: mSvgIcons.Duotone.general.crossSquare,
    },
};

@Component({
    selector: 'lt-portlet-tool',
    templateUrl: './portlet-tool.component.html',
})
export class PortletToolComponent implements OnInit {
    @Input()
    public type: PortletToolType | undefined;

    @Input()
    public icon: string | undefined;

    @Input()
    public tooltip: string | undefined;

    @Output()
    public action = new EventEmitter<{ type?: PortletToolType; state?: string }>();

    public parent: PortletHeadComponent | undefined;

    private state: string | undefined;

    constructor() {
        // noop
    }

    public ngOnInit() {
        if (this.type && config[this.type] && !this.icon) {
            if (this.type === 'fullscreen') {
                this.icon = config[this.type].on;
            } else {
                this.icon = config[this.type].icon;
            }
        }
    }

    public handleClick() {
        this.handleToolAction(this);
        this.action.emit({ type: this.type, state: this.state });
    }

    public get root(): PortletComponent | undefined {
        if (this.parent) {
            return this.parent.parent;
        }
        return undefined;
    }

    public get linkClass(): string {
        const builder = ClassBuilder.create(`btn-tool tool-${this.type}`);
        if (this.type === 'toggle') builder.css('rotate-180');
        return builder.toString();
    }

    public handleToolAction(tool: PortletToolComponent) {
        if (!this.type || !this.root) return;
        const conf = this.root.options.tools[this.type];
        switch (tool.type) {
            case 'toggle': {
                this.root.toggle();
                if (this.state === 'collapse') {
                    this.state = 'expand';
                    this.tooltip = conf.expand;
                } else {
                    this.tooltip = conf.collapse;
                    this.state = 'collapse';
                }
                break;
            }
            case 'fullscreen':
                this.root.fullscreenPortlet();
                if (this.state === 'on') {
                    this.state = 'off';
                    this.tooltip = conf.off;
                    this.icon = config[tool.type].on;
                } else {
                    this.tooltip = conf.on;
                    this.state = 'on';
                    this.icon = config[tool.type].off;
                }
                break;
            case 'reload':
                this.root.reloadPortlet();
                break;
            case 'remove':
                this.root.remove();
                break;
        }
    }

    public initTooltip() {
        if (!this.type || !this.root) return;
        const conf = this.root.options.tools[this.type];
        switch (this.type) {
            case 'toggle':
                this.tooltip = conf.collapse;
                this.state = 'collapse';
                this.root.collapsible = true;
                break;
            case 'fullscreen':
                this.tooltip = conf.off;
                this.state = 'off';
                break;
            case 'reload':
                this.tooltip = conf;
                break;
            case 'remove':
                this.tooltip = conf;
                break;
        }
    }
}
