import { ChangeDetectorRef, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { BlockUI, SvgIcons } from '@devacfr/bootstrap';
import { UpdateUserProfile, UserProfile, UserService, UserSettings } from '@devacfr/core';
import { ImageInputComponent } from '@devacfr/forms';
import { I18nService, NotifierService } from '@devacfr/layout';
import _ from 'lodash-es';
import { Subscription } from 'rxjs';
import { finalize } from 'rxjs/operators';

@Component({
    selector: 'app-profile',
    templateUrl: './profile.component.html',
})
export class ProfileComponent implements OnInit, OnDestroy {
    @ViewChild('Avatar', { static: true })
    public avatarComponent: ImageInputComponent | undefined;

    public message: string | undefined;

    public formControl: FormGroup;

    private _subscription = new Subscription();

    private _avatarSource: string | undefined;

    constructor(
        public svgIcons: SvgIcons,
        private _fb: FormBuilder,
        private _userService: UserService,
        private _notifierService: NotifierService,
        private _i18nService: I18nService,
        private _cd: ChangeDetectorRef
    ) {
        this.formControl = UserProfile.createForm(this._fb);
        this._subscription.add(this.avatarSource.valueChanges.subscribe((value) => this.avatarSettingChange(value)));
    }

    public ngOnInit() {
        this.refresh();
    }

    public ngOnDestroy(): void {
        this._subscription.unsubscribe();
    }

    public get username(): FormControl {
        return this.formControl.get('username') as FormControl;
    }

    public get avatarUrl(): FormControl {
        return this.formControl.get('avatarUrl') as FormControl;
    }

    public get displayName(): FormControl {
        return this.formControl.get('displayName') as FormControl;
    }

    public get email(): FormControl {
        return this.formControl.get('email') as FormControl;
    }

    public get contactPhone(): FormControl {
        return this.formControl.get('contactPhone') as FormControl;
    }

    public get officeLocation(): FormControl {
        return this.formControl.get('officeLocation') as FormControl;
    }

    public get readOnly(): boolean {
        return this.formControl.get('readOnly')?.value;
    }

    public get settings(): FormGroup {
        return this.formControl.get('settings') as FormGroup;
    }

    public get avatarSource(): FormControl {
        return this.formControl.get('settings.avatarSource') as FormControl;
    }

    public updateProfile() {
        if (this.formControl.invalid) {
            return;
        }
        if (!this.readOnly) {
            const updateUser: UpdateUserProfile = {
                username: this.username.value,
                displayName: this.displayName.value,
                email: this.email.value,
            };
            this._userService.updateProfile(updateUser).subscribe({
                next: (user) => {
                    this.updateForm(user);
                    this._notifierService.success('Your profile has been saved!');
                },
                error: (err) => {
                    this._notifierService.error(err);
                },
            });
        }
    }

    public refresh() {
        const block = new BlockUI('#m-portlet_account_profile').block();
        this.message = undefined;
        this._userService
            .getProfile()
            .pipe(finalize(() => block.release()))
            .subscribe({
                next: (user) => this.updateForm(user),
                error: (err) => {
                    this._notifierService.error(err);
                },
            });
    }

    public avatarSettingChange(avatarSource: string): void {
        if (avatarSource === this._avatarSource) {
            return;
        }
        const block = new BlockUI('#m-portlet_account_profile');
        const settings: UserSettings = this.settings.value;
        settings.avatarSource = avatarSource;
        this.message = undefined;
        this._userService
            .updateSettings(this.username.value, settings)
            .pipe(finalize(() => block.release()))
            .subscribe({
                next: (user) => this.updateForm(user),
                error: (err) => {
                    this._notifierService.error(err);
                },
            });
    }

    protected updateForm(user: UserProfile) {
        if (user.readOnly) {
            this.message = this._i18nService.instant('account.profile.messages.readonly');
        } else {
            this.message = undefined;
        }
        this._avatarSource = user.settings.avatarSource;
        this.formControl.setValue(user, { emitEvent: false });
        user.readOnly ? this.formControl.disable() : this.formControl.enable();
        user.settings.avatarSource !== 'Disable' ? this.avatarUrl.disable() : this.avatarUrl.enable();
        this.settings.enable();
        this.username.disable();
        this._cd.detectChanges();
    }

    public uploadAvatar(file: File): void {
        this._userService.uploadAvatar(this.username.value, file).subscribe({
            next: () => this.avatarComponent?.complete(),
            error: (err) => {
                this._notifierService.error(err);
            },
        });
    }

    public removeAvatar(): void {
        this.avatarUrl.setValue(null);
        this._userService.deleteAvatar(this.username.value).subscribe({
            next: _.noop,
            error: (err) => {
                this._notifierService.error(err);
            },
        });
    }

    public get isLocalAvatar() {
        return (
            (typeof this.avatarUrl.value === 'string' && this.avatarUrl.value.indexOf('rest/api/users') >= 0) ||
            this.avatarUrl.value instanceof File
        );
    }
}
