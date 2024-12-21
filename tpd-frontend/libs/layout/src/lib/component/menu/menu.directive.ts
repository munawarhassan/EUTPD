import { Directive, ElementRef, HostListener, OnDestroy, OnInit, Renderer2 } from '@angular/core';
import { BaseMenuComponent } from './base-menu.component';

@Directive({
    selector: '[ltMenu]',
    exportAs: 'ltMenu',
    // eslint-disable-next-line @angular-eslint/no-inputs-metadata-property
    inputs: ['menuType', 'options'],
})
export class MenuDirective extends BaseMenuComponent implements OnInit, OnDestroy {
    constructor(protected _element: ElementRef, protected _renderer: Renderer2) {
        super(_element, _renderer);
    }

    ngOnInit(): void {
        this.init(this.menuType);
    }

    @HostListener('mouseout', ['$event'])
    public handleMouseOut(event: Event) {
        super.handleMouseOut(event);
    }

    @HostListener('mouseover', ['$event'])
    public handleMouseOver(event: Event) {
        super.handleMouseOver(event);
    }

    @HostListener('click', ['$event'])
    public handleClick(event: Event) {
        super.handleClick(event);
    }

    @HostListener('document:click', ['$event'])
    public handleDropdown(event: Event) {
        super.handleDropdown(event);
    }
}
