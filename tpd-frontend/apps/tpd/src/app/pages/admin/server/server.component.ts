import { Component } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';

@Component({
    selector: 'app-server',
    templateUrl: './server.component.html',
})
export class ServerComponent {
    constructor(public svgIcons: SvgIcons) {}
}
