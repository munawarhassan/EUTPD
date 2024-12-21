import { Component, Input } from '@angular/core';
import { BreakpointValue } from '@devacfr/util';
import { uniqueId } from 'lodash-es';

@Component({
    selector: 'lt-portlet-nav-dropdown-item',
    template: `
        <a href="#" class="btn btn-icon" data-bs-toggle="dropdown">
            <i *ngIf="!useSVG()" [class]="icon"></i>
            <inline-svg *ngIf="useSVG()" [src]="icon" [size]="iconSize"></inline-svg>
        </a>
        <ul [id]="dropdownId" class="dropdown-menu">
            <ng-content></ng-content>
        </ul>
    `,
})
export class PortletNavDropdownItemComponent {
    public dropdownId = uniqueId('dropdownItem');

    @Input()
    public icon: string | undefined;

    @Input()
    public iconSize: BreakpointValue = '1';

    constructor() {
        // noop
    }

    public useSVG() {
        return this.icon && this.icon.endsWith('.svg');
    }
}
