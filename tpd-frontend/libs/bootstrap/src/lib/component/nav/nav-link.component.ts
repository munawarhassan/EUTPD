import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    ElementRef,
    EventEmitter,
    HostBinding,
    Input,
    OnDestroy,
    Output,
    Renderer2,
    ViewChild,
} from '@angular/core';
import { ClassBuilder } from '@devacfr/util';
import { Tab } from 'bootstrap';
import { RootTab } from './typing';

@Component({
    // eslint-disable-next-line @angular-eslint/component-selector
    selector: 'nav-link',
    templateUrl: './nav-link.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NavLinkComponent implements OnDestroy {
    @HostBinding('class')
    public get cssClass(): string {
        const builder = ClassBuilder.create('nav-item');
        return builder.toString();
    }

    @HostBinding('role')
    public role = 'presentation';

    @Input()
    public title: string | undefined;

    @Input()
    public link: unknown[] | string | undefined;

    @Input()
    public icon: string | undefined;

    @Input()
    public iconClass = '';

    @Input()
    public active = false;

    @Input()
    public target: string | undefined;

    public get linkClass(): string {
        const builder = ClassBuilder.create('nav-link');
        if (this.active) builder.css('active');
        if (this.parent) builder.css(this.parent.linkClass);
        return builder.toString();
    }

    @Input()
    public parent: RootTab | undefined;

    @Output()
    public showTarget = new EventEmitter<Event>();

    @Output()
    public shown = new EventEmitter<Event>();

    @Output()
    public hideTarget = new EventEmitter<Event>();

    @Output()
    public hidden = new EventEmitter<Event>();

    private _bsElement: Tab | undefined;

    private _toggle: ElementRef | undefined;

    @ViewChild('toggle')
    public set toggle(value: ElementRef | undefined) {
        this._toggle = value;
        if (this._toggle) {
            this._bsElement = this.getOrcreateCollapse();
        }
    }

    constructor(private _element: ElementRef, private _renderer: Renderer2, private _cd: ChangeDetectorRef) {}

    ngOnDestroy(): void {
        this._bsElement?.dispose();
    }

    public get useSVG() {
        return this.icon && this.icon.endsWith('.svg');
    }

    private getOrcreateCollapse(force = false): Tab | undefined {
        const el = this._toggle?.nativeElement as Element;
        if (!el) return undefined;
        if (this._bsElement && force) {
            this._bsElement.dispose();
        }

        const tab = new Tab(el);
        this._renderer.listen(el, 'click', (event: Event) => {
            event.preventDefault();
            if (this._bsElement) {
                this._bsElement.show();
            }
        });
        if (this.active) {
            tab.show();
        }
        this._renderer.listen(this._element.nativeElement, 'show.bs.tab', (event) => {
            this.active = true;
            this.showTarget.emit(event);
            this._cd.detectChanges();
        });
        this._renderer.listen(this._element.nativeElement, 'shown.bs.tab', (event) => this.shown.emit(event));
        this._renderer.listen(this._element.nativeElement, 'hide.bs.tab', (event) => {
            this.active = false;
            this.hideTarget.emit(event);
            this._cd.detectChanges();
        });
        this._renderer.listen(this._element.nativeElement, 'hidden.bs.tab', (event) => this.hidden.emit(event));

        return tab;
    }
}
