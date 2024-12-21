import {
    AfterViewInit,
    Component,
    ComponentFactoryResolver,
    OnDestroy,
    OnInit,
    ViewChild,
    ViewContainerRef,
} from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { BlockUI, SvgIcons } from '@devacfr/bootstrap';
import { User, UserService } from '@devacfr/core';
import { EqualToValidator } from '@devacfr/forms';
import { NotifierService } from '@devacfr/layout';
import { Subject, Subscription } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { AlertProfileComponent } from '../alert-profile.component';

@Component({
    selector: 'app-change-password-profile',
    templateUrl: './change-password-profile.component.html',
})
export class ChangePasswordProfileComponent implements OnInit, AfterViewInit, OnDestroy {
    public account: User | undefined;
    public readonly = new Subject<boolean>();

    @ViewChild('alert', { read: ViewContainerRef })
    public alert: ViewContainerRef | undefined;

    public formControl: FormGroup;

    private _subscription = new Subscription();

    constructor(
        public svgIcons: SvgIcons,
        private _fb: FormBuilder,
        private _componentFactoryResolver: ComponentFactoryResolver,
        private _router: Router,
        private _userService: UserService,
        private _notifierService: NotifierService
    ) {
        this.formControl = this._fb.group({
            password: [null, [Validators.required, Validators.maxLength(50)]],
            newpassword: [null, [Validators.required, Validators.minLength(5), Validators.maxLength(50)]],
            confirmpassword: [null, [Validators.required, EqualToValidator.equalTo('newpassword')]],
        });
    }

    public ngOnInit() {
        this._subscription.add(
            this.readonly.subscribe((status) => {
                if (status) {
                    this.displayAlert(
                        'You <strong>cannot</strong> edit your password ' +
                            'as it is stored in a read-only user directory.'
                    );
                } else {
                    this.alert?.clear();
                }
            })
        );
    }

    public ngAfterViewInit(): void {
        this.refresh();
    }

    ngOnDestroy(): void {
        this._subscription.unsubscribe();
    }

    public changePassword() {
        if (this.formControl.invalid) {
            return;
        }
        const data = this.formControl.value;
        this._userService.changePassword(data.password, data.newpassword).subscribe({
            next: () => {
                this._notifierService.success('Password changed!');
                this._router.navigate(['account', 'profile']);
            },
            error: (err) => {
                this._notifierService.error(err);
            },
        });
    }

    public get password(): FormControl {
        return this.formControl.get('password') as FormControl;
    }

    public get newpassword(): FormControl {
        return this.formControl.get('newpassword') as FormControl;
    }

    public get confirmpassword(): FormControl {
        return this.formControl.get('confirmpassword') as FormControl;
    }

    public refresh() {
        const block = new BlockUI('#m-portlet_change_password').block();
        this._userService
            .getAccount()
            .pipe(finalize(() => block.release()))
            .subscribe({
                next: (data) => {
                    const a = data;
                    this.readonly.next(!a.updatable);
                    this.account = a;
                },
                error: (err) => {
                    this._notifierService.error(err);
                },
            });
    }

    private displayAlert(message: string) {
        if (!this.alert) return;
        this.alert.clear();
        const componentFactory = this._componentFactoryResolver.resolveComponentFactory(AlertProfileComponent);
        const componentRef = this.alert.createComponent(componentFactory);
        componentRef.instance.message = message;
    }
}
