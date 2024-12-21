import { Component, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, NavigationStart, Router } from '@angular/router';
import { BreadcrumbService } from '@devacfr/bootstrap';
import { GeneralInfo, InfoService, StartupService } from '@devacfr/core';
import { EventManager, I18nService } from '@devacfr/layout';
import { BsModalService } from 'ngx-bootstrap/modal';
import { Subscription } from 'rxjs';
import { AboutDialogComponent } from './pages/about/about-dialog.component';
import { TeamDialogComponent } from './pages/team/team-dialog.component';
@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit, OnDestroy {
    public isProduction = false;
    public isDev = false;
    public isQA = false;

    public splashScreenEnable = true;
    private _unsubscribe = new Subscription();
    constructor(
        public i18n: I18nService,
        public breadcrumbService: BreadcrumbService, // not remove, need initialize on load page
        private _router: Router,
        private _startupService: StartupService,
        private _modalService: BsModalService,
        private _eventManager: EventManager,
        private _infoService: InfoService
    ) {
        // enforce retrieve current user
        this._unsubscribe.add(
            this._router.events.subscribe((event) => {
                if (event instanceof NavigationStart) {
                    this._startupService.handle(this._router).subscribe(() => {
                        /* noop */
                    });
                }
                if (event instanceof NavigationEnd) {
                    this.splashScreenEnable = false;
                }
            })
        );
        this._unsubscribe.add(
            this._eventManager.subscribe('about', {
                next: () => this.openModal(AboutDialogComponent),
            })
        );
        this._unsubscribe.add(
            this._eventManager.subscribe('team', {
                next: () => this.openModal(TeamDialogComponent),
            })
        );
    }

    ngOnInit(): void {
        this._infoService.getInfo().subscribe((info: GeneralInfo) => {
            this.isDev = info.environment === 'development';
            this.isProduction = info.environment === 'production';
            this.isQA = info.environment === 'qa';
        });
    }

    ngOnDestroy() {
        this._unsubscribe.unsubscribe();
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    public openModal(component: any) {
        this._modalService.show(component, {
            animated: true,
            backdrop: true,
            class: 'modal-lg modal-dialog-centered',
        });
    }
}
