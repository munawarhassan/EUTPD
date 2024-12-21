import { Component } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';

@Component({
    selector: 'app-admin-overview',
    templateUrl: './overview.component.html',
})
export class AdminOverviewComponent {
    constructor(public svgIcons: SvgIcons) {}
}
