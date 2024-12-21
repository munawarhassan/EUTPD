import {
    AfterViewInit,
    Component,
    ContentChildren,
    ElementRef,
    EventEmitter,
    HostBinding,
    HostListener,
    Inject,
    Input,
    OnDestroy,
    OnInit,
    Output,
    PLATFORM_ID,
    QueryList,
    ViewEncapsulation,
} from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { StickyDirective, SvgIcons } from '@devacfr/bootstrap';
import { ClassBuilder } from '@devacfr/util';
import { Observable, Subscription } from 'rxjs';
import { PortletEvent, PortletOption } from '.';
import { PortletToolComponent } from './portlet-tool.component';
import { PortletComponent } from './portlet.component';
import { PortletHeadModeType } from './typing';

@Component({
    selector: 'lt-portlet-head',
    templateUrl: './portlet-head.component.html',
    encapsulation: ViewEncapsulation.None,
})
export class PortletHeadComponent implements OnInit, AfterViewInit, OnDestroy {
    @Input()
    public description: string | undefined;

    @Input()
    public icon: string | undefined;

    @Input()
    public iconCLass = 'svg-icon-1';

    @Input()
    public tabClass = '';

    @Input()
    public title: string | undefined;

    @Input()
    public mode: PortletHeadModeType[] | PortletHeadModeType | undefined;

    // enable sticky portlet header
    @Input()
    public sticky = false;

    // enable loading to display
    @Input() viewLoading$: Observable<boolean> | undefined;
    viewLoading = false;

    @HostBinding('attr.ltSticky')
    private stickyDirective: StickyDirective;

    @ContentChildren(PortletToolComponent)
    public tools: QueryList<PortletToolComponent> | undefined;

    @Output()
    public stickyOn = new EventEmitter<PortletEvent>();

    @Output()
    public stickyOff = new EventEmitter<PortletEvent>();

    public stickyEnabled = false;

    private _parent: PortletComponent | undefined;
    private _cssClass: string | undefined;
    private subscriptions = new Subscription();

    constructor(
        public svgIcons: SvgIcons,
        public sanitized: DomSanitizer,
        public elementRef: ElementRef,
        @Inject(PLATFORM_ID) private platformId: string
    ) {
        this.stickyDirective = new StickyDirective(this.elementRef, this.platformId);
    }

    public get parent(): PortletComponent | undefined {
        return this._parent;
    }

    public set parent(parent: PortletComponent | undefined) {
        this._parent = parent;
        if (this.tools) {
            this.tools.forEach((tool) => {
                tool.parent = this;
                tool.initTooltip();
            });
        }
    }

    ngOnInit() {
        if (this.sticky) {
            this.stickyDirective.ngOnInit();
        }
    }

    ngAfterViewInit(): void {
        if (this.sticky) {
            this.updateStickyPosition();
            this.stickyDirective.ngAfterViewInit();
        }

        // initialize loading dialog
        if (this.viewLoading$) {
            this.subscriptions.add(this.viewLoading$.subscribe((res) => this.toggleLoading(res)));
        }
    }

    public ngOnDestroy(): void {
        this.subscriptions.unsubscribe();
        if (this.sticky) {
            this.stickyDirective.ngOnDestroy();
        }
    }

    public get options(): PortletOption | undefined {
        return this.parent?.options;
    }

    @HostBinding('class')
    @Input()
    public get class(): string {
        const builder = ClassBuilder.create('portlet-head');
        if (this._cssClass) builder.css(this._cssClass);
        if (this.mode) {
            builder.flag('portlet-head--', ...this.mode);
        }
        return builder.toString();
    }

    public set class(value: string) {
        this._cssClass = value;
    }

    public get offset(): number {
        let offset = this.options?.sticky?.offset;
        if (typeof offset === 'function') {
            offset = offset.call(this);
        }
        return offset as number;
    }

    @HostListener('window:resize', ['$event'])
    onResize() {
        this.updateStickyPosition();
    }

    @HostListener('window:scroll', ['$event'])
    onScroll() {
        this.updateStickyPosition();
    }

    updateStickyPosition() {
        if (this.sticky) {
            setTimeout(() => {
                this.stickyDirective.marginTop = this.offset;
            });
        }
    }

    toggleLoading(_incomingValue: boolean) {
        this.viewLoading = _incomingValue;
    }

    public useSVG(icon: string | undefined): boolean {
        return icon != null && icon.endsWith('.svg');
    }
}
