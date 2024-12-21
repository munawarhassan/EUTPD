import {
    AfterContentInit,
    Component,
    ContentChildren,
    ElementRef,
    HostBinding,
    Input,
    OnChanges,
    QueryList,
    Renderer2,
    SimpleChanges,
} from '@angular/core';
import { ClassBuilder } from '@devacfr/util';
import { RootTab } from '.';
import { NavLinkComponent } from './nav-link.component';
import { TabPaneDirective } from './tab-pane.directive';

@Component({
    selector: 'lt-nav',
    exportAs: 'ltNav',
    templateUrl: './nav.component.html',
})
export class NavComponent implements RootTab, AfterContentInit, OnChanges {
    @Input()
    public linkClass = '';

    // @HostBinding('class')
    @Input()
    public class: string | null = null;

    @HostBinding('attr.role')
    public get role(): string | null {
        return this.tabset && this.tabset.length ? 'tablist' : null;
    }

    @ContentChildren(NavLinkComponent)
    public links: QueryList<NavLinkComponent> | undefined;

    @ContentChildren(TabPaneDirective)
    public tabset: QueryList<TabPaneDirective> | undefined;

    public get navClass(): string | null {
        const build = ClassBuilder.create('nav');
        if (this._navClass) build.css(this._navClass);
        if (this.tabset && this.tabset.length) build.css('nav-tabs');
        return build.toString();
    }
    public set navClass(value: string | null) {
        this._navClass = value;
    }

    private _navClass: string | null = null;
    constructor(private _elementRef: ElementRef, private _renderer: Renderer2) {}

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.class && changes.class.currentValue != changes.class.previousValue) {
            this._navClass = this.class;
            const el = this._elementRef.nativeElement as HTMLElement;
            this._renderer.removeAttribute(el, 'class');
        }
    }

    public ngAfterContentInit(): void {
        this.links?.forEach((item) => (item.parent = this));
        this.tabset?.forEach((item) => (item.parent = this));
    }
}
