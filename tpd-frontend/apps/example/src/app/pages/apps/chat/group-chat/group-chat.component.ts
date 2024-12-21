import { Component, HostBinding } from '@angular/core';

@Component({
    selector: 'app-group-chat',
    templateUrl: './group-chat.component.html',
    styleUrls: ['./group-chat.component.scss'],
})
export class GroupChatComponent {
    @HostBinding('class') class = 'd-flex flex-column flex-lg-row';
}
