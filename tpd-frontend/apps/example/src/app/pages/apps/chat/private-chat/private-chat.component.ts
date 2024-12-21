import { Component, HostBinding } from '@angular/core';

@Component({
    selector: 'app-private-chat',
    templateUrl: './private-chat.component.html',
    styleUrls: ['./private-chat.component.scss'],
})
export class PrivateChatComponent {
    @HostBinding('class') class = 'd-flex flex-column flex-lg-row';
}
