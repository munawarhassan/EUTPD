import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import * as _ from 'lodash-es';
import { Observable, Subject } from 'rxjs';

export interface Locale {
    lang: string;
    data: Record<string, unknown | string>;
}

export interface LanguageFlag {
    lang: string;
    country: string;
    flag: string;
    active?: boolean;
}

const LOCALIZATION_LOCAL_STORAGE_KEY = 'language';

export const LanguageFlags: LanguageFlag[] = [
    {
        lang: 'en',
        country: 'English',
        flag: './assets/media/flags/GB.svg',
    },
    {
        lang: 'ch',
        country: 'Mandarin',
        flag: './assets/media/flags/CN.svg',
    },
    {
        lang: 'es',
        country: 'Spanish',
        flag: './assets/media/flags/ES.svg',
    },
    {
        lang: 'jp',
        country: 'Japanese',
        flag: './assets/media/flags/JP.svg',
    },
    {
        lang: 'de',
        country: 'German',
        flag: './assets/media/flags/DE.svg',
    },
    {
        lang: 'fr',
        country: 'French',
        flag: './assets/media/flags/FR.svg',
    },
];

@Injectable({
    providedIn: 'root',
})
export class I18nService {
    private _currentLang!: string;

    private langIds: string[] = [];

    private changed$ = new Subject<string>();

    constructor(private _translateService: TranslateService) {
        this.init();
    }

    private init(): void {
        this._translateService.addLangs(['en', 'fr']);
        const lang = localStorage.getItem(LOCALIZATION_LOCAL_STORAGE_KEY);
        this._translateService.setDefaultLang('en');
        if (lang) {
            this.use(lang);
        } else {
            const browserLang = this.browserLanguage;
            this.use(browserLang);
        }
    }

    public get changed(): Observable<string> {
        return this.changed$;
    }

    /**
     * Returns a translation instantly from the internal state of loaded translation.
     * All rules regarding the current language, the preferred language of even
     * fallback languages will be used except any promise handling.
     *
     * @param key
     * @param interpolateParams
     * @returns {string}
     */
    public instant(
        key: string | string[],
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        interpolateParams?: any
    ): string {
        const msg = this._translateService.instant(key, interpolateParams);
        return msg ? msg : key;
    }

    /**
     * Allows to change the language used in I18n.
     *
     * @param langKey the country code associate to language
     */
    public use(langKey: string) {
        let lang: string | null = langKey;
        if (lang) {
            const minus = lang.indexOf('-');
            if (minus >= 0) {
                lang = lang.substring(0, minus);
            }
        } else {
            lang = localStorage.getItem(LOCALIZATION_LOCAL_STORAGE_KEY);
            if (!lang) {
                lang = this.browserLanguage;
            }
        }
        this._currentLang = lang;
        this._translateService.use(this._currentLang);
        localStorage.setItem(LOCALIZATION_LOCAL_STORAGE_KEY, lang);
        this.changed$.next(this._currentLang);
    }

    loadTranslations(...args: Locale[]): void {
        const locales = [...args];

        locales.forEach((locale) => {
            // use setTranslation() with the third argument set to true
            // to append translations instead of replacing them
            this._translateService.setTranslation(locale.lang, locale.data, true);
            this.langIds.push(locale.lang);
        });

        // add new languages to the list
        this._translateService.addLangs(this.langIds);
    }

    /**
     *  Gets the current language used.
     */
    public get currentLang(): string {
        return this._currentLang;
    }

    public get languages(): string[] {
        return this._translateService.getLangs();
    }

    public get browserLanguage(): string {
        // TODO need implement the language setting in  functional test
        const browserLang = this._translateService.getBrowserLang();
        return browserLang && browserLang.match(/en|fr/) ? browserLang : 'en';
    }

    public getLanguageFlags(): LanguageFlag[] {
        const langs = this.languages;
        const flags = _.cloneDeep(LanguageFlags);
        return flags.filter((language) => langs.indexOf(language.lang) >= 0);
    }
}
