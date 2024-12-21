import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MenuModule } from '@devacfr/layout';
import { TopbarComponent } from './topbar.component';

import { TopbarUserComponent } from './topbar-user/topbar-user.component';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { TopbarNotificationsComponent } from './topbar-notifications/topbar-notifications.component';
import { InlineSVGModule } from '@devacfr/bootstrap';

@NgModule({
    imports: [CommonModule, RouterModule, TranslateModule, MenuModule, InlineSVGModule],
    exports: [TopbarComponent],
    declarations: [TopbarComponent, TopbarUserComponent, TopbarNotificationsComponent],
    providers: [],
})
export class TopbarModule {}
