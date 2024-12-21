import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DrawerOptions, SvgIcons } from '@devacfr/bootstrap';
import { User, UserService } from '@devacfr/core';
import { NotifierService } from '@devacfr/layout';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-aside-user',
    templateUrl: './aside-user.component.html',
    styleUrls: ['./aside-user.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AsideUserComponent implements OnInit, OnDestroy {
    public user: User | undefined;

    public asideDrawerOptions: Partial<DrawerOptions> = {
        name: 'aside',
        activate: { default: true, lg: false },
        overlay: true,
        width: '300px',
        direction: 'start',
        toggle: '#lt_aside_mobile_toggle',
    };

    private subscriptions = new Subscription();

    constructor(
        public activedRoute: ActivatedRoute,
        public svgIcons: SvgIcons,
        private _userService: UserService,
        private _notifierService: NotifierService,
        private _cd: ChangeDetectorRef
    ) {
        this.subscriptions.add(this._userService.currentUserChanged.subscribe(() => this.refresh()));
    }

    public ngOnInit(): void {
        document.body.style.setProperty('--lt-aside-width', '350px');
        this.refresh();
    }

    public ngOnDestroy(): void {
        document.body.style.removeProperty('--lt-aside-width');
        this.subscriptions.unsubscribe();
    }

    private refresh(): void {
        this._userService.getAccount().subscribe({
            next: (user) => {
                this.user = user;
                this._cd.markForCheck();
            },
            error: (err) => this._notifierService.error(err),
        });
    }
}
