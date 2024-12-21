import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { SvgIcons } from '@devacfr/bootstrap';
import { CreateUser, UserAdminService, UserDirectories } from '@devacfr/core';
import { EqualToValidator, FormSelectOptionType } from '@devacfr/forms';
import { NotifierService } from '@devacfr/layout';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-user-create',
    templateUrl: './user-create.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminUserCreateComponent implements OnDestroy {
    public confirmPasswordText: string | undefined;

    private subscriptions = new Subscription();

    public formControl: FormGroup;

    public userDirectories: FormSelectOptionType[];

    constructor(
        public svgIcons: SvgIcons,
        private _fb: FormBuilder,
        private _route: ActivatedRoute,
        private _router: Router,
        private _userAdminService: UserAdminService,
        private _notifierService: NotifierService
    ) {
        this.userDirectories = UserDirectories.map<FormSelectOptionType>((d) => ({
            name: d.description,
            value: d.name,
        }));
        this.formControl = CreateUser.createForm(this._fb, this.newUser());
        this.formControl.addControl('confirmPassword', this._fb.control(null, [EqualToValidator.equalTo('password')]));
        this.subscriptions.add(
            this._router.events.subscribe((event) => {
                if (event instanceof NavigationEnd) {
                    // Trick the Router into believing it's last link wasn't previously loaded
                    this._router.navigated = false;
                }
            })
        );
    }

    public ngOnDestroy(): void {
        this.subscriptions.unsubscribe();
    }

    public get directory(): FormControl {
        return this.formControl.get('directory') as FormControl;
    }

    public get notify(): FormControl {
        return this.formControl.get('notify') as FormControl;
    }

    public save(event: Event): void {
        const target = event.currentTarget as Element;
        let action = 'save-exit';
        if (target.getAttribute('id') === 'm_user_save_new_btn') {
            action = 'save-new';
        }

        if (this.formControl.invalid) {
            return;
        }

        const createUser = this.formControl.value as CreateUser;
        delete (createUser as any).confirmPassword;
        if (!this.passwordRequired()) {
            createUser.notify = false;
        }

        this._userAdminService.createUser(createUser).subscribe({
            next: () => {
                this._notifierService.successWithKey('users.create.notify.created', { user: createUser.username });
                if (action === 'save-exit') {
                    this._router.navigate(['../view', createUser.username], { relativeTo: this._route });
                } else {
                    this.formControl.reset(this.newUser(), { emitEvent: false });
                    this._router.navigate(['../create'], {
                        relativeTo: this._route,
                        queryParams: { refresh: new Date().getTime() },
                    });
                }
            },
            error: (err) => this._notifierService.error(err),
        });
    }

    public passwordRequired(): boolean {
        const dir = this._userAdminService.getDirectory(this.directory.value);
        if (dir) return dir.passwordRequired;
        return false;
    }

    private newUser(): Partial<CreateUser> {
        return {
            directory: 'Internal',
            addToDefaultGroup: true,
            notify: true,
        };
    }
}
