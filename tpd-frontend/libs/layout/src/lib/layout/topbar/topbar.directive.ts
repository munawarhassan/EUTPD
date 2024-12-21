import { Directive, HostBinding } from '@angular/core';

@Directive({
    selector: '[ltTopbar]',
})
export class TopbarDirective {
    @HostBinding('class')
    public class = 'd-flex align-items-center flex-shrink-0';
}
