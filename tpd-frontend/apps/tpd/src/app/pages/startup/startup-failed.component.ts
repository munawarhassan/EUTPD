import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApplicationEvent, ApplicationStateInfo, StartupService } from '@devacfr/core';
import { NotifierService } from '@devacfr/layout';

@Component({
    selector: 'app-startup-failed',
    templateUrl: './startup-failed.component.html',
    styleUrls: ['./startup-failed.component.scss'],
})
export class StartupFailedComponent implements OnInit {
    public applicationInfo: ApplicationStateInfo = {};
    public events: ApplicationEvent[] = [];

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
        this._startupService.events().subscribe((events) => {
            if (!events || events.length === 0) {
                this._router.navigate(['/']);
            }
            this.events = events;
        });
    }

    public reportError() {
        if (this.events && this.events.length > 0) {
            const event = this.events[0];
            const error = {
                message: `${event.level.description}: ${event.description}`,
                throwable: event.exception,
            };
            // IssueCollectorService.reportBug(error);
        }
    }
}
