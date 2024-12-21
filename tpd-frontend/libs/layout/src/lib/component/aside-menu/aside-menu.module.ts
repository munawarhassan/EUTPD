import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { DirectivesModule, InlineSVGModule } from '@devacfr/bootstrap';
import { MenuModule } from '../menu';
import { AsideMenuComponent } from './aside-menu.component';

@NgModule({
    imports: [CommonModule, MenuModule, DirectivesModule, InlineSVGModule],
    exports: [AsideMenuComponent],
    declarations: [AsideMenuComponent],
    providers: [],
})
export class AsideMenuModule {}
