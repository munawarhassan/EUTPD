import { Component, Input } from '@angular/core';
import { IconUserModel } from '../icon-user.model';

@Component({
    selector: 'app-card2',
    templateUrl: './card2.component.html',
})
export class Card2Component {
    @Input() icon = '';
    @Input() badgeColor = '';
    @Input() status = '';
    @Input() statusColor = '';
    @Input() title = '';
    @Input() description = '';
    @Input() date = '';
    @Input() budget = '';
    @Input() progress = 50;
    @Input() users: Array<IconUserModel> = [];
}
