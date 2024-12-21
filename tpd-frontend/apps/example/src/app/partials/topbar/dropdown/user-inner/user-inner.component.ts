import { Component, OnDestroy, OnInit } from '@angular/core';
import { I18nService, LanguageFlag, LanguageFlags } from '@devacfr/layout';
import { Observable, of, Subscription } from 'rxjs';

@Component({
    selector: 'app-user-inner',
    templateUrl: './user-inner.component.html',
})
export class UserInnerComponent implements OnInit, OnDestroy {
    public language = LanguageFlags[0];
    user$: Observable<any>;
    public langs: LanguageFlag[];
    private unsubscribe: Subscription[] = [];

    constructor(private _i18nService: I18nService) {
        this.langs = this._i18nService.getLanguageFlags();
        this.user$ = of({
            id: 1,
            username: 'admin',
            password: 'demo',
            email: 'admin@demo.com',
            authToken: 'auth-token-8f3ae836da744329a6f93bf20594b5cc',
            refreshToken: 'auth-token-f8c137a2c98743f48b643e71161d90aa',
            roles: [1], // Administrator
            pic: './assets/media/avatars/150-2.jpg',
            fullname: 'Sean S',
            firstname: 'Sean',
            lastname: 'Stark',
            occupation: 'CEO',
            companyName: 'Keenthemes',
            phone: '456669067890',
            language: 'en',
            timeZone: 'International Date Line West',
            website: 'https://keenthemes.com',
            emailSettings: {
                emailNotification: true,
                sendCopyToPersonalEmail: false,
                activityRelatesEmail: {
                    youHaveNewNotifications: false,
                    youAreSentADirectMessage: false,
                    someoneAddsYouAsAsAConnection: true,
                    uponNewOrder: false,
                    newMembershipApproval: false,
                    memberRegistration: true,
                },
                updatesFromKeenthemes: {
                    newsAboutKeenthemesProductsAndFeatureUpdates: false,
                    tipsOnGettingMoreOutOfKeen: false,
                    thingsYouMissedSindeYouLastLoggedIntoKeen: true,
                    newsAboutMetronicOnPartnerProductsAndOtherServices: true,
                    tipsOnMetronicBusinessProducts: true,
                },
            },
            communication: {
                email: true,
                sms: true,
                phone: false,
            },
            address: {
                addressLine: 'L-12-20 Vertex, Cybersquare',
                city: 'San Francisco',
                state: 'California',
                postCode: '45000',
            },
            socialNetworks: {
                linkedIn: 'https://linkedin.com/admin',
                facebook: 'https://facebook.com/admin',
                twitter: 'https://twitter.com/admin',
                instagram: 'https://instagram.com/admin',
            },
        });
    }

    ngOnInit(): void {
        this.setLanguage(this._i18nService.currentLang);
    }

    logout() {
        document.location.reload();
    }

    selectLanguage(lang: string) {
        this.setLanguage(lang);
        // document.location.reload();
    }

    setLanguage(lang: string) {
        this._i18nService.use(lang);
        this.langs.forEach((language: LanguageFlag) => {
            if (language.lang === lang) {
                language.active = true;
                this.language = language;
            } else {
                language.active = false;
            }
        });
    }

    ngOnDestroy() {
        this.unsubscribe.forEach((sb) => sb.unsubscribe());
    }
}
