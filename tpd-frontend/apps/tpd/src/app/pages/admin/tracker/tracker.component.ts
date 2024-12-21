import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { TrackerService, TrackingActivity } from '@devacfr/core';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-tracker',
    templateUrl: './tracker.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TrackerComponent implements OnInit, OnDestroy {
    public activities: TrackingActivity[] = [];
    public subscription = new Subscription();

    constructor(public svgIcons: SvgIcons, private _trackerService: TrackerService, private _cd: ChangeDetectorRef) {}

    public ngOnInit(): void {
        // This controller uses a Websocket connection to receive user
        // activities in real-time.
        this.subscription.add(
            this._trackerService.watch().subscribe((activity) => {
                this.showActivity(activity);
            })
        );
    }

    public ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }

    public showActivity(activity: TrackingActivity) {
        let existingActivity = false;
        for (let index = 0; index < this.activities.length; index++) {
            if (this.activities[index].sessionId === activity.sessionId) {
                existingActivity = true;
                if (activity.page === 'logout') {
                    this.activities.splice(index, 1);
                } else {
                    this.activities[index] = activity;
                }
            }
        }
        if (!existingActivity && activity.page !== 'logout') {
            this.activities.push(activity);
        }
        this._cd.markForCheck();
    }
}
