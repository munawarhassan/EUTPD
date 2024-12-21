import { Component, HostBinding, Input } from '@angular/core';

@Component({
    selector: 'app-card3',
    templateUrl: './card3.component.html',
})
export class Card3Component {
    @Input() color = '';
    @Input() avatar = '';
    @Input() online = false;
    @Input() name = '';
    @Input() job = '';
    @Input() avgEarnings = '';
    @Input() totalEarnings = '';
    @HostBinding('class') class = 'card';
}
