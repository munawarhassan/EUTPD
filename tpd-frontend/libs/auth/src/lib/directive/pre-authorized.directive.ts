import { Directive, Input, OnDestroy, OnInit, TemplateRef, ViewContainerRef } from '@angular/core';
import { Subscription } from 'rxjs';
import { PermissionsService } from '../permissions.service';
import { PrincipalService } from '../principal.service';

/**
 * @whatItDoes Conditionally includes an HTML element if current user has any
 * of the authorities passed as the `expression`.
 *
 * @howToUse
 * ```
 *     <some-element *preAuthorized="'ROLE_ADMIN'">...</some-element>
 *
 *     <some-element *preAuthorized="['ROLE_ADMIN', 'ROLE_USER']">...</some-element>
 * ```
 */
@Directive({
    // eslint-disable-next-line @angular-eslint/directive-selector
    selector: '[preAuthorized]',
})
export class PreAuthorizedDirective implements OnInit, OnDestroy {
    private authorities: string[] | undefined;

    private subscription = new Subscription();

    constructor(
        private _principalService: PrincipalService,
        private _permissions: PermissionsService,
        private templateRef: TemplateRef<unknown>,
        private viewContainerRef: ViewContainerRef
    ) {}
    public ngOnInit(): void {
        this.subscription.add(
            this._principalService.identity().subscribe({
                next: (principal) => {
                    if (principal || !this.authorities || this.authorities.length === 0) {
                        this.updateView();
                    }
                },
            })
        );
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
    }

    @Input()
    set preAuthorized(value: string | string[]) {
        if (value) {
            this.authorities = typeof value === 'string' ? [value] : value;
        } else {
            this.authorities = [];
        }
    }

    private updateView(): void {
        let result = true;
        this.viewContainerRef.clear();
        if (this.authorities && this.authorities.length > 0) {
            result = this._permissions.hasAnyAuthority(this._principalService.userToken, ...this.authorities);
        }
        if (result) {
            this.viewContainerRef.createEmbeddedView(this.templateRef);
        }
    }
}
