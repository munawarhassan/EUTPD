import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { DirectivesModule } from '../../directive';
import { InlineSVGModule } from '../inline-svg';
import { NavItemDirective } from './nav-item.directive';

import { NavLinkComponent } from './nav-link.component';
import { NavComponent } from './nav.component';
import { TabPaneDirective } from './tab-pane.directive';

@NgModule({
    imports: [CommonModule, RouterModule, InlineSVGModule, DirectivesModule],
    exports: [NavLinkComponent, NavItemDirective, NavComponent, TabPaneDirective],
    declarations: [NavLinkComponent, NavItemDirective, NavComponent, TabPaneDirective],
    providers: [],
})
export class NavModule {}
