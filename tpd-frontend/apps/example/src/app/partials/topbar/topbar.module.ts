import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MenuModule } from '@devacfr/layout';
import { TopbarComponent } from './topbar.component';

import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { NotificationsInnerComponent } from './dropdown/notifications-inner/notifications-inner.component';
import { QuickLinksInnerComponent } from './dropdown/quick-links-inner/quick-links-inner.component';
import { UserInnerComponent } from './dropdown/user-inner/user-inner.component';
import { InlineSVGModule } from '@devacfr/bootstrap';

@NgModule({
    imports: [CommonModule, RouterModule, TranslateModule, MenuModule, InlineSVGModule],
    exports: [TopbarComponent],
    declarations: [TopbarComponent, UserInnerComponent, NotificationsInnerComponent, QuickLinksInnerComponent],
    providers: [],
})
export class TopbarModule {}
