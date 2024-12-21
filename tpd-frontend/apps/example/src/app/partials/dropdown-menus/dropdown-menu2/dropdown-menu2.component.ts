import { Component, HostBinding } from '@angular/core';
import { uniqueId } from 'lodash-es';

@Component({
    selector: 'app-dropdown-menu2',
    templateUrl: './dropdown-menu2.component.html',
})
export class DropdownMenu2Component {
    @HostBinding('id')
    public id = uniqueId();

    @HostBinding('class') class =
        'menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-600 menu-state-bg-light-primary fw-bold w-200px';
}
