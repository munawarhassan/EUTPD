import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '@devacfr/auth';
import { User, UserAdminService, UserService } from '@devacfr/core';
import { I18nService, LanguageFlag, LanguageFlags } from '@devacfr/layout';
import { combineLatest, Observable, of, Subscription } from 'rxjs';
import { map } from 'rxjs/operators';

@Component({
    selector: 'app-topbar-user',
    templateUrl: './topbar-user.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TopbarUserComponent implements OnInit, OnDestroy {
    public language = LanguageFlags[0];
    public user$: Observable<User>;
    public highestGlobalPermission$: Observable<string> = of();
    public langs: LanguageFlag[];
    private subscriptions = new Subscription();

    constructor(
        private _router: Router,
        private _authService: AuthService,
        private _userService: UserService,
        private _userAdminService: UserAdminService,
        private _i18nService: I18nService
    ) {
        this.langs = this._i18nService.getLanguageFlags();
        this.user$ = combineLatest([this._userService.currentUserChanged, this._userService.getAccount()]).pipe(
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            map(([, user]) => {
                this.highestGlobalPermission$ = this._userAdminService
                    .getHighestGlobalPermission(user.username)
                    .pipe(map((permission) => permission.name));
                return user;
            })
        );
    }

    ngOnInit(): void {
        this.setLanguage(this._i18nService.currentLang);
    }

    ngOnDestroy() {
        this.subscriptions.unsubscribe();
    }

    logout() {
        this._router.navigate(['/logout']);
    }

    selectLanguage(lang: string) {
        this.setLanguage(lang);
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
}
