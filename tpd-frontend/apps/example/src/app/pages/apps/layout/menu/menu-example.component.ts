import { Component } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { DefaultMenuConfig } from '../../../../../_config/menu.config';

@Component({
    selector: 'app-menu-example',
    templateUrl: './menu-example.component.html',
})
export class MenuExampleComponent {
    public menuItems = DefaultMenuConfig.header.items;

    constructor(public svgIcons: SvgIcons) {}
}
