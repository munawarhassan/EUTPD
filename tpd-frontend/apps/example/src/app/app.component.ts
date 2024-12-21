import { Component, OnDestroy } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { I18nService } from '@devacfr/layout';
import { getCSSVariableValue } from '@devacfr/util';
import { setTheme } from 'ngx-bootstrap/utils';
import { Subscription } from 'rxjs';
import { enLang, frLang } from './i18n';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnDestroy {
    public loadingBarColor;

    public splashScreenEnable = true;
    private _subscription = new Subscription();

    constructor(private _router: Router, private _i18nService: I18nService) {
        // register translations
        this._i18nService.loadTranslations(enLang, frLang);
        this.loadingBarColor = getCSSVariableValue('--bs-primary');
        setTheme('bs5');
        this._subscription.add(
            this._router.events.subscribe((event) => {
                if (event instanceof NavigationEnd) {
                    this.splashScreenEnable = false;
                }
            })
        );
    }

    ngOnDestroy(): void {
        this._subscription.unsubscribe();
    }
}
