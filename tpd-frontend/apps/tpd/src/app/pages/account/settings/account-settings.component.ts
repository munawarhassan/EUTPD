import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { BlockUI } from '@devacfr/bootstrap';
import { UserProfile, UserService } from '@devacfr/core';
import { I18nService, NotifierService } from '@devacfr/layout';
import { finalize } from 'rxjs/operators';

@Component({
    selector: 'app-account-settings',
    templateUrl: './account-settings.component.html',
})
export class AccountSettingsComponent implements OnInit {
    public formControl: FormGroup;
    constructor(
        private _fb: FormBuilder,
        private _userService: UserService,
        private _notifierService: NotifierService,
        private _I18nService: I18nService,
        private _cd: ChangeDetectorRef
    ) {
        this.formControl = UserProfile.createForm(this._fb);
    }

    ngOnInit() {
        this.refresh();
    }

    public get username(): FormControl {
        return this.formControl.get('username') as FormControl;
    }

    public get avatarUrl(): FormControl {
        return this.formControl.get('avatarUrl') as FormControl;
    }

    public get settings(): FormGroup {
        return this.formControl.get('settings') as FormGroup;
    }

    public get avatarSource(): FormControl {
        return this.formControl.get('settings.avatarSource') as FormControl;
    }

    public get langKey(): FormControl {
        return this.formControl.get('settings.langKey') as FormControl;
    }

    public refresh() {
        const block = new BlockUI('#m-portlet_account_settings').block();
        this._userService
            .getProfile()
            .pipe(finalize(() => block.release()))
            .subscribe({
                next: (user) => {
                    this.formControl.setValue(user, { emitEvent: false });
                    if (!this.langKey.value) this.langKey.setValue(this._I18nService.currentLang);
                    this._cd.detectChanges();
                },
                error: (err) => {
                    this._notifierService.error(err);
                },
            });
    }

    public removeAvatar(): void {
        this._userService.deleteAvatar(this.username.value).subscribe(
            () => {
                this.refresh();
            },
            (err) => {
                this._notifierService.error(err);
            }
        );
    }

    public get isLocalAvatar() {
        if (this.avatarUrl.value) return this.avatarUrl.value.indexOf('rest/api/users') >= 0;
        return false;
    }

    public updateSettings(): void {
        const block = new BlockUI('#m-portlet_account_settings').block();
        this._userService
            .updateSettings(this.username.value, this.settings.value)
            .pipe(finalize(() => block.release()))
            .subscribe({
                next: (user) => {
                    this.formControl.setValue(user, { emitEvent: false });
                    this._cd.detectChanges();
                },
                error: (err) => {
                    this._notifierService.error(err);
                },
            });
    }

    public avatarSettingChange(): void {
        this.updateSettings();
    }

    public languageChanged(lang: string) {
        this.langKey.setValue(lang);
        this.updateSettings();
    }
}
