import { AfterContentInit, Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApplicationStateInfo, LifecycleState, StartupProgress, StartupService } from '@devacfr/core';
import { NotifierService } from '@devacfr/layout';
import { BsColor, Progress } from '@devacfr/util';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-startup',
    templateUrl: './startup.component.html',
    styleUrls: ['./startup.component.scss'],
})
export class StartupComponent implements OnInit, OnDestroy, AfterContentInit {
    public year: string = new Date().getFullYear().toString();

    public applicationInfo: ApplicationStateInfo = {};
    public progress = new Progress('Waiting...', 100);
    public progressType: BsColor = 'primary';
    public progressWaiting = true;

    private startupProgress: StartupProgress | undefined;
    private _subscriptions = new Subscription();

    constructor(
        private _notifierService: NotifierService,
        private _startupService: StartupService,
        private _router: Router
    ) {}

    public ngOnInit() {
        this._startupService.info().subscribe({
            next: (info) => (this.applicationInfo = info),
            error: (err) => {
                this._notifierService.error(err);
            },
        });
    }

    public ngOnDestroy(): void {
        this._subscriptions.unsubscribe();
    }

    public ngAfterContentInit(): void {
        this.startupProgress = new StartupProgress(this._startupService);
        this._subscriptions.add(
            this.startupProgress.start().subscribe({
                next: (lifecycle) => {
                    if (lifecycle.progress.percentage === 100 || lifecycle.state === LifecycleState.Started) {
                        this._router.navigate(['/']);
                    } else if (lifecycle.state === LifecycleState.Failed) {
                        this._router.navigate(['/startup/failed']);
                    } else {
                        this.updateProgress(lifecycle.progress);
                    }
                },
                error: (err) => {
                    this._notifierService.error(err);
                },
            })
        );
    }

    public updateProgress(progress: Progress) {
        this.progress = progress;
        this.progressWaiting = false;
        this.progressType = progress.percentage < 100 ? 'primary' : 'success';
    }
}
