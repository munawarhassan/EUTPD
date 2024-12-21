import { Component, HostBinding, Input } from '@angular/core';

@Component({
    selector: 'app-card5',
    templateUrl: './card5.component.html',
})
export class Card5Component {
    @Input() image = '';
    @Input() title = '';
    @Input() description = '';
    @Input() status: 'up' | 'down' = 'up';
    @Input() statusValue: number | undefined;
    @Input() statusDesc = '';
    @Input() progress = 100;
    @Input() progressType = '';
    @HostBinding('class') class = 'card h-100';
}
