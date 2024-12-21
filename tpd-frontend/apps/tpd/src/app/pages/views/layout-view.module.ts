import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { DirectivesModule } from '@devacfr/bootstrap';
import { LayoutModule } from '@devacfr/layout';
import { FooterModule } from '@tpd/app/partials/footer/footer.module';
import { TopbarModule } from '@tpd/app/partials/topbar/topbar.module';
import { LayoutViewComponent } from './layout-view.component';

@NgModule({
    imports: [CommonModule, RouterModule, LayoutModule, DirectivesModule, FooterModule, TopbarModule],
    exports: [LayoutViewComponent],
    declarations: [LayoutViewComponent],
    providers: [],
})
export class LayoutViewModule {}
