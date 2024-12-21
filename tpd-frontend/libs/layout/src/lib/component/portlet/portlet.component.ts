import {
    AfterViewInit,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    ContentChild,
    ElementRef,
    EventEmitter,
    HostBinding,
    Input,
    OnDestroy,
    Output,
    QueryList,
    Renderer2,
    ViewEncapsulation,
} from '@angular/core';
import { ClassBuilder } from '@devacfr/util';
import { uniqueId } from 'lodash-es';
import { Subscription } from 'rxjs';
import { DefaultPortletOptions, Portlet, PortletEvent, PortletModeType, PortletOption } from './typing';
import { PortletBodyComponent } from './portlet-body.component';
import { PortletFootComponent } from './portlet-foot.component';
import { PortletHeadComponent } from './portlet-head.component';
import { PortletTabDirective } from './portlet-tab.directive';
import { PortletToolComponent } from './portlet-tool.component';
import _ from 'lodash-es';

export type BeforeRemoveCallback = () => boolean | Promise<boolean>;

@Component({
    selector: 'lt-portlet',
    exportAs: 'portlet',
    templateUrl: './portlet.component.html',
    styleUrls: ['./portlet.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PortletComponent implements AfterViewInit, OnDestroy, Portlet {
    @Input()
    public collapsed = false;

    @Input()
    public fullscreen = false;

    @Input()
    public mode: PortletModeType[] | PortletModeType | undefined;

    @Input()
    public set options(value: PortletOption) {
        this._options = Object.assign({}, DefaultPortletOptions, value || {});
    }

    public get options(): PortletOption {
        return this._options;
    }

    @HostBinding('class')
    public get class(): string {
        const stylebuilder = ClassBuilder.create('portlet');
        if (this.mode) {
            const ar = this.mode instanceof Array ? this.mode : [this.mode];
            stylebuilder.flag('portlet--', ...ar);
        }
        if (this.tabset && this.tabset.length) {
            stylebuilder.css('portlet--tabs');
        }
        if (this.removed) {
            stylebuilder.css('portlet--remove');
        }
        if (this.collapsible) {
            stylebuilder.css('collapsible');
        }
        if (this.collapsed) {
            stylebuilder.css('collapsed');
        }
        if (this.fullscreen) {
            stylebuilder.css('portlet--fullscreen');
        }
        if (this.portletHead && this.portletHead.stickyEnabled) {
            stylebuilder.css('portlet--sticky');
        }
        return stylebuilder.toString();
    }

    @Output()
    public beforeCollapse = new EventEmitter<PortletEvent>();
    @Output()
    public afterCollapse = new EventEmitter<PortletEvent>();

    @Output()
    public beforeExpand = new EventEmitter<PortletEvent>();
    @Output()
    public afterExpand = new EventEmitter<PortletEvent>();

    @Output()
    public beforeFullscreenOff = new EventEmitter<PortletEvent>();
    @Output()
    public afterFullscreenOff = new EventEmitter<PortletEvent>();

    @Output()
    public beforeFullscreenOn = new EventEmitter<PortletEvent>();
    @Output()
    public afterFullscreenOn = new EventEmitter<PortletEvent>();

    @Input()
    public beforeRemove: BeforeRemoveCallback | undefined;

    @Output()
    public afterRemove = new EventEmitter<PortletEvent>();

    @Output()
    public reload = new EventEmitter<PortletEvent>();

    public collapsible = false;

    @ContentChild(PortletHeadComponent)
    private portletHead: PortletHeadComponent | undefined;

    @ContentChild(PortletBodyComponent)
    private portletBody: PortletBodyComponent | undefined;

    @ContentChild(PortletFootComponent)
    private portletFoot: PortletFootComponent | undefined;

    private subsciptions: Subscription[] = [];

    private removed = false;
    private _options: PortletOption = DefaultPortletOptions;

    public wrapperBodyId = uniqueId('portletWrapperBody');

    constructor(private _cd: ChangeDetectorRef, private _elementRef: ElementRef, private _renderer: Renderer2) {
        // noop
    }

    public get tabset(): QueryList<PortletTabDirective> | undefined {
        if (this.portletBody) {
            return this.portletBody.tabset;
        }
        return undefined;
    }

    public ngAfterViewInit(): void {
        setTimeout(() => {
            if (this.portletHead) {
                this.portletHead.parent = this;
            }
            if (this.portletBody) {
                this.portletBody.parent = this;
            }
        });
    }

    public ngOnDestroy(): void {
        this.subsciptions.forEach((sub) => {
            sub.unsubscribe();
        });
    }

    public handleToolAction(tool: PortletToolComponent) {
        switch (tool.type) {
            case 'toggle':
                this.toggle();
                break;
            case 'fullscreen':
                this.fullscreenPortlet();
                break;
            case 'reload':
                break;
            case 'remove':
                this.remove();
                break;
        }
    }

    public toggle(): void {
        if (this.collapsed) {
            this.expand();
        } else {
            this.collapse();
        }
    }

    public unFullscreen() {
        return this.fullscreenPortlet('off');
    }

    public fullscreenPortlet(mode?: 'off' | 'on') {
        const element = this._elementRef.nativeElement as HTMLElement;
        const body$ = document.body;
        const body = this.portletBody ? this.portletBody.elementRef.nativeElement : null;
        const foot = this.portletFoot ? this.portletFoot.elementRef.nativeElement : null;
        const head = this.portletHead ? this.portletHead.elementRef.nativeElement : null;

        if (mode === 'off' || this.fullscreen) {
            this.beforeFullscreenOff.emit({ target: this });

            this._renderer.removeClass(body$, 'portlet--fullscreen');
            this._renderer.removeClass(element, 'portlet--fullscreen');
            this.fullscreen = false;
            if (this.portletBody) {
                this._renderer.setStyle(body, 'margin-bottom', '');
                this._renderer.setStyle(body, 'margin-top', '');
            }
            if (this.portletFoot) {
                foot.css('margin-top', '');
            }
            this.markForCheck();
            this.afterFullscreenOff.emit({ target: this });
        } else {
            this.beforeFullscreenOn.emit({ target: this });

            this.fullscreen = true;
            this._renderer.addClass(element, 'portlet--fullscreen');
            this._renderer.addClass(body$, 'portlet--fullscreen');

            if (this.portletFoot) {
                // const height1 = parseInt(foot.css('height'), 10);
                // const height2 = parseInt(foot.css('height'), 10)
                //  + parseInt(head.css('height'), 10);
                // body.css('margin-bottom', height1 + 'px');
                // body.css('margin-top', '-' + height2 + 'px');
            }
            this.markForCheck();
            this.afterFullscreenOn.emit({ target: this });
        }
        this.expand();
    }

    public reloadPortlet(): void {
        this.markForCheck();
        this.reload.emit({ target: this });
    }

    public remove() {
        if (this.beforeRemove) {
            const result = this.beforeRemove();
            Promise.resolve(result).then((remove) => {
                if (remove) {
                    this.removePortlet();
                }
            });
        } else {
            this.removePortlet();
        }
    }

    public display(): void {
        this.removed = false;
        this.markForCheck();
    }

    protected markForCheck(): void {
        this._cd.markForCheck();
    }

    private removePortlet() {
        const body = document.body;
        if (body.classList.contains('portlet--fullscreen') && this.fullscreen) {
            this.unFullscreen();
        }
        this.removed = true;
        this.markForCheck();
        this.afterRemove.emit({ target: this });
    }

    /**
     * Collapse
     */
    private collapse(): void {
        this.beforeCollapse.emit({ target: this });

        // slideUp(this.portletBody?.elementRef.nativeElement, this.options.bodyToggleSpeed, () => {
        //     this.afterCollapse.emit({ target: this });
        // });

        this.collapsed = true;
        this._cd.markForCheck();
        this.afterCollapse.emit({ target: this });
    }

    /**
     * Expand
     */
    private expand(): void {
        this.beforeExpand.emit({ target: this });

        // slideDown(this.portletBody?.elementRef.nativeElement, this.options.bodyToggleSpeed, () => {
        //     this.afterExpand.emit({ target: this });
        // });
        this.collapsed = false;
        this._cd.markForCheck();
        this.afterExpand.emit({ target: this });
    }
}
