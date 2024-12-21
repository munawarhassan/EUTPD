import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ChatRoutingModule } from './chat-routing.module';
import { ChatComponent } from '../chat/chat.component';
import { PrivateChatComponent } from './private-chat/private-chat.component';
import { GroupChatComponent } from './group-chat/group-chat.component';
import { DrawerChatComponent } from './drawer-chat/drawer-chat.component';

import { MenuModule } from '@devacfr/layout';
import { DirectivesModule, InlineSVGModule } from '@devacfr/bootstrap';
import { CardsModule, ChatInnerModule } from '../../../partials';
import { DropdownMenusModule } from '../../../partials/dropdown-menus/dropdown-menus.module';
import { Select2Module } from '@devacfr/forms';

@NgModule({
    declarations: [ChatComponent, PrivateChatComponent, GroupChatComponent, DrawerChatComponent],
    imports: [
        CommonModule,
        ChatRoutingModule,
        DropdownMenusModule,
        DirectivesModule,
        MenuModule,
        ChatInnerModule,
        CardsModule,
        InlineSVGModule,
        Select2Module,
    ],
})
export class ChatModule {}
