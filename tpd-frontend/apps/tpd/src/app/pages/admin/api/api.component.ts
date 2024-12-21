import { Component } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';

@Component({
    selector: 'app-api',
    templateUrl: './api.component.html',
})
export class ApiComponent {
    constructor(public svgIcons: SvgIcons) {}
}
