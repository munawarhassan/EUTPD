import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { SetupService } from '@devacfr/core';
import { I18nService, NotifierService, WizardStepComponent } from '@devacfr/layout';
import { lastValueFrom, of, Subscription } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';

@Component({
    selector: 'app-setup-confirmation',
    templateUrl: './setup-confirmation.component.html',
})
export class SetupConfirmationComponent implements OnInit, OnDestroy {
    @Input()
    public step: WizardStepComponent | undefined;

    @Input()
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    public data: Record<string, any> | undefined;

    private _subscription = new Subscription();

    constructor(
        private _setupService: SetupService,
        private _I18nService: I18nService,
        private _notifierService: NotifierService
    ) {}

    ngOnInit(): void {
        if (this.step) {
            this.step.beforeComplete = () => {
                return lastValueFrom(
                    this._setupService.completeSetup().pipe(
                        switchMap(() => of(true)),
                        catchError((err) => {
                            this._notifierService.error(err);
                            return of(false);
                        })
                    )
                );
                // return of(true).toPromise();
            };
        }
    }

    ngOnDestroy(): void {
        this._subscription.unsubscribe();
    }

    public getCountry(key: string): string {
        if (!key) return '';
        const l = this._I18nService.getLanguageFlags().find((lang) => lang.lang === key);
        return l ? l.country : '';
    }
}
