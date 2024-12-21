import { CommonModule } from '@angular/common';
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { PortletBodyComponent } from './portlet-body.component';
import { PortletFootComponent } from './portlet-foot.component';
import { PortletHeadComponent } from './portlet-head.component';
import { PortletNavDropdownItemComponent } from './portlet-nav-dropdown-item.component';
import { PortletNavItemComponent } from './portlet-nav-item.component';
import { PortletTabDirective } from './portlet-tab.directive';
import { PortletToolComponent } from './portlet-tool.component';
import { PortletComponent } from './portlet.component';
import { DirectivesModule, InlineSVGModule } from '@devacfr/bootstrap';

@NgModule({
    imports: [CommonModule, DirectivesModule, InlineSVGModule],
    exports: [
        PortletComponent,
        PortletBodyComponent,
        PortletHeadComponent,
        PortletFootComponent,
        PortletToolComponent,
        PortletNavItemComponent,
        PortletNavDropdownItemComponent,
        PortletTabDirective,
    ],
    declarations: [
        PortletComponent,
        PortletBodyComponent,
        PortletHeadComponent,
        PortletToolComponent,
        PortletNavItemComponent,
        PortletNavDropdownItemComponent,
        PortletFootComponent,
        PortletTabDirective,
    ],
    providers: [],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class PortletModule {}
