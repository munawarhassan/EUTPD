import { Component, HostBinding, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { StartupService } from '@devacfr/core';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Component({
    selector: 'app-unavailable-error',
    templateUrl: './unavailable.component.html',
    styleUrls: ['./unavailable.component.scss'],
})
export class UnavailableErrorComponent implements OnInit, OnDestroy {
    @HostBinding('class')
    public classes = 'd-flex flex-column flex-root vh-100';

    private subscription = new Subscription();

    public constructor(private _router: Router, private _startupService: StartupService) {
        // noop
    }

    public ngOnInit() {
        this.subscription.add(
            interval(5000)
                .pipe(switchMap(() => this._startupService.isStarted()))
                .subscribe((started) => (started ? this._router.navigate(['/']) : ''))
        );
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
    }
}
