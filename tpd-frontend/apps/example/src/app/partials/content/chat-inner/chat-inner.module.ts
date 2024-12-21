import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChatInnerComponent } from './chat-inner.component';
import { DirectivesModule, InlineSVGModule } from '@devacfr/bootstrap';

@NgModule({
    declarations: [ChatInnerComponent],
    imports: [CommonModule, InlineSVGModule, DirectivesModule],
    exports: [ChatInnerComponent],
})
export class ChatInnerModule {}
