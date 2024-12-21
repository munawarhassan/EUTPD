import { Directive, HostBinding } from '@angular/core';
import { ClassBuilder } from '@devacfr/util';

// eslint-disable-next-line @angular-eslint/directive-selector
@Directive({ selector: '[navItem]' })
export class NavItemDirective {
    @HostBinding('class')
    public get cssClass(): string {
        const builder = ClassBuilder.create('nav-item');
        return builder.toString();
    }
}
