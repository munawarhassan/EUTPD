import { ChangeDetectionStrategy, Component, HostBinding, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { LayoutConfig } from '../../layout-config';
import { LayoutService } from '../../service';

@Component({
    selector: 'lt-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HeaderComponent implements OnDestroy {
    @HostBinding('id')
    public id = 'lt_header';

    @HostBinding('class')
    public class = 'header';

    public logo: string | undefined;
    public containerClass = 'container-fluid';

    private _subscriptions = new Subscription();

    constructor(private _layout: LayoutService, private _router: Router) {
        this._subscriptions.add(_layout.configUpdated().subscribe(this.init.bind(this)));
    }

    private init(config: LayoutConfig): void {
        this.reset();
        this.logo = config.header.logo;
        if (config.header.width === 'fluid') {
            this.containerClass = 'container-fluid';
        } else {
            this.containerClass = 'container-xxl';
        }

        if (config.header.fixed.desktop) {
            document.body.classList.add('header-fixed');
        }

        if (config.header.fixed.tabletAndMobile) {
            document.body.classList.add('header-tablet-and-mobile-fixed');
        }
    }

    private reset(): void {
        document.body.classList.remove('header-fixed', 'header-tablet-and-mobile-fixed');
    }

    ngOnDestroy() {
        this.reset();
        this._subscriptions.unsubscribe();
    }
}
