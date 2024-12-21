import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { DrawerOptions } from '@devacfr/bootstrap';
import { Subscription } from 'rxjs';
import { MenuComponentType, MenuConfigService } from '../../component/menu';

@Component({
    selector: 'lt-menu-header',
    templateUrl: './menu-header.component.html',
})
export class MenuHeaderComponent implements OnInit, OnDestroy {
    @Input()
    public menu: MenuComponentType = { type: 'dropdown' };

    public headerDrawerOptions: Partial<DrawerOptions> = {
        name: 'header-menu',
        activate: { default: true, lg: false },
        overlay: true,
        width: '300px',
        direction: 'end',
        toggle: '#lt_header_menu_mobile_toggle',
    };

    private _subscriptions = new Subscription();
    constructor(private _menuService: MenuConfigService) {
        this._subscriptions.add(this._menuService.menuUpdated().subscribe((config) => (this.menu = config.header)));
    }

    ngOnInit(): void {
        document.body.classList.add('header-menu-enabled');
    }

    ngOnDestroy() {
        document.body.classList.remove('header-menu-enabled');
        this._subscriptions.unsubscribe();
    }
}
