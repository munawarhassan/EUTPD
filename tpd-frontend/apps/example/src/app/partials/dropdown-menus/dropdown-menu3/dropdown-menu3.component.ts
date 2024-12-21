import { Component, HostBinding } from '@angular/core';
import { uniqueId } from 'lodash-es';

@Component({
    selector: 'app-dropdown-menu3',
    templateUrl: './dropdown-menu3.component.html',
})
export class DropdownMenu3Component {
    @HostBinding('id')
    public id = uniqueId();

    @HostBinding('class') class =
        'menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-bold w-200px py-3';
}
