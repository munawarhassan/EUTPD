import { Component, HostBinding } from '@angular/core';
import { uniqueId } from 'lodash-es';

@Component({
    selector: 'app-dropdown-menu1',
    templateUrl: './dropdown-menu1.component.html',
})
export class DropdownMenu1Component {
    @HostBinding('id')
    public id = uniqueId();

    @HostBinding('class')
    public class = 'menu-sub menu-sub-dropdown w-250px w-md-300px';
}
