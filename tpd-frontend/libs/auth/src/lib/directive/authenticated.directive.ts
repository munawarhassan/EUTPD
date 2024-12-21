import { Directive, Input, OnDestroy, TemplateRef, ViewContainerRef } from '@angular/core';
import { Subscription } from 'rxjs';
import { PrincipalService } from '../principal.service';

@Directive({
    // eslint-disable-next-line @angular-eslint/directive-selector
    selector: '[authenticated]',
})
export class AuthenticatedDirective implements OnDestroy {
    private _authenticated: boolean | undefined;
    private _flag = false;

    private subscription = new Subscription();

    constructor(
        private _principalService: PrincipalService,
        private templateRef: TemplateRef<unknown>,
        private viewContainerRef: ViewContainerRef
    ) {
        this.subscription.add(
            this._principalService.observable().subscribe({
                next: this.updateView.bind(this),
            })
        );
    }

    public ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }

    @Input()
    set authenticated(value: boolean) {
        this._flag = value;
        this.updateView();
    }

    private updateView(): void {
        if (this._authenticated === undefined) {
            this._authenticated = this._principalService.isAuthenticated();
            this.viewContainerRef.clear();
            if (this._authenticated === this._flag) {
                this.viewContainerRef.createEmbeddedView(this.templateRef);
            }
        }
    }
}
