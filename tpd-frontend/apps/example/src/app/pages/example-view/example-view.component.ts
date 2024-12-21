import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { DrawerOptions } from '@devacfr/bootstrap';
import { MenuComponentType, MenuConfigService } from '@devacfr/layout';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-example-view',
    templateUrl: 'example-view.component.html',
})
export class ExampleViewComponent implements OnInit, OnDestroy {
    public appPreviewDocsUrl = 'environment.appPreviewDocsUrl';
    public currentDateStr: string = new Date().getFullYear().toString();

    public asideDrawerOptions: Partial<DrawerOptions> = {
        name: 'aside',
        activate: { default: true, lg: false },
        overlay: true,
        width: '300px',
        direction: 'start',
        toggle: '#lt_aside_mobile_toggle',
    };

    public menu: MenuComponentType = { type: 'accordion' };

    private _subscriptions = new Subscription();

    constructor(private _menuService: MenuConfigService, private _cd: ChangeDetectorRef) {}

    ngOnInit(): void {
        this._subscriptions.add(
            this._menuService.menuUpdated().subscribe((config) => {
                this.menu = config.aside;
                this._cd.markForCheck();
            })
        );
    }

    ngOnDestroy() {
        this._subscriptions.unsubscribe();
    }
}
