import { Component, OnDestroy, Renderer2, ViewEncapsulation } from '@angular/core';
import { I18nService, LayoutService } from '@devacfr/layout';

@Component({
    selector: 'app-auth',
    templateUrl: './auth.component.html',
    styleUrls: ['./auth.component.scss'],
    encapsulation: ViewEncapsulation.None,
})
export class AuthComponent implements OnDestroy {
    public headerLogo: string;

    public currentLanguage: string;

    public backgroundImage: string | undefined;

    constructor(private _i18n: I18nService, private _lcs: LayoutService, private _renderer: Renderer2) {
        this.currentLanguage = this._i18n.currentLang;
        this.headerLogo = this._lcs.config.auth.logo;

        this.backgroundImage = this._lcs.config.auth.backgroundImage;

        this._renderer.setStyle(document.body, 'background-image', 'url(' + this.backgroundImage + ')');
        this._renderer.setStyle(document.body, 'background-size', 'cover');
    }

    ngOnDestroy() {
        this._renderer.setStyle(document.body, 'background-image', '');
        this._renderer.setStyle(document.body, 'background-size', '');
    }

    public get useSVG() {
        return this.headerLogo && this.headerLogo.endsWith('.svg');
    }
}
