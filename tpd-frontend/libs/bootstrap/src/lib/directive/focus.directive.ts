import { AfterViewInit, Directive, ElementRef, Input } from '@angular/core';

@Directive({
    selector: 'input[ltFocus]',
})
export class FocusDirective implements AfterViewInit {
    @Input()
    public ltFocus = true;

    constructor(public element: ElementRef<HTMLElement>) {}

    ngAfterViewInit(): void {
        // ExpressionChangedAfterItHasBeenCheckedError: Expression has changed after it was checked.
        if (this.ltFocus) {
            setTimeout(() => this.element.nativeElement.focus(), 0);
        }
    }
}
