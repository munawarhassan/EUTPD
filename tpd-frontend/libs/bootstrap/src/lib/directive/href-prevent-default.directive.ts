import { Directive, ElementRef, HostListener, Input } from '@angular/core';

@Directive({
    // eslint-disable-next-line @angular-eslint/directive-selector
    selector: '[href]',
})
export class HrefPreventDefaultDirective {
    @Input()
    public href: string | undefined;

    constructor(private el: ElementRef) {}

    @HostListener('click', ['$event'])
    public onClick(event: Event) {
        this.preventDefault(event);
    }

    public preventDefault(event: Event) {
        if (this.href?.length === 0 || (this.href && this.href.startsWith('#'))) {
            event.preventDefault();
        }
    }
}
