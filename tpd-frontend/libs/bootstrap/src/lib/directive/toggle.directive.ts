import { Directive, ElementRef, Input, OnDestroy } from '@angular/core';
import { DefaultToggleOptions, ToggleComponent, ToggleOptions } from './_ToggleComponent';

@Directive({
    selector: '[ltToggle]',
    exportAs: 'ltToggle',
})
export class ToggleDirective implements OnDestroy {
    @Input()
    public toggleOptions: ToggleOptions | undefined;

    private component: ToggleComponent;

    constructor(private _element: ElementRef) {
        this.component = new ToggleComponent(this._element.nativeElement, DefaultToggleOptions);
    }

    ngOnDestroy(): void {
        this.component.destroy();
    }
}
