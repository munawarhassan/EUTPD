import { Component, Input } from '@angular/core';
import { IconUserModel } from '../icon-user.model';

@Component({
    selector: 'app-user-list',
    templateUrl: './user-list.component.html',
    styleUrls: ['./user-list.component.scss'],
})
export class UserListComponent {
    @Input() users: Array<IconUserModel> = [];
}
