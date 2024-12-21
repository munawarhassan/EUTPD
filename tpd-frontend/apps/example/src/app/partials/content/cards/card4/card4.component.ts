import { Component, HostBinding, Input } from '@angular/core';

@Component({
    selector: 'app-card4',
    templateUrl: './card4.component.html',
})
export class Card4Component {
    @Input() icon = '';
    @Input() title = '';
    @Input() description = '';
    @HostBinding('class') class = 'card h-100';
}
