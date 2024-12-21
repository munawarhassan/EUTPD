import { ChangeDetectorRef, Component, OnDestroy } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { Metrics, MonitoringService } from '@devacfr/core';
import { NotifierService } from '@devacfr/layout';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';

interface Thread {
    percentage?: number;
    count?: number;
}

interface Threads {
    count: number;
    runnable: Thread;
    waiting: Thread;
    blocked: Thread;
    free: Thread;
}

@Component({
    selector: 'app-metrics-threads',
    templateUrl: './metrics-threads.component.html',
})
export class MetricsThreadsComponent implements OnDestroy {
    public threads: Threads = {
        count: 0,
        runnable: {},
        waiting: {},
        blocked: {},
        free: {},
    };

    private subscriptions;

    constructor(
        public svgIcons: SvgIcons,
        private _cd: ChangeDetectorRef,
        private _monitoringService: MonitoringService,
        private _notifierService: NotifierService
    ) {}

    public ngOnDestroy(): void {
        this.stop();
    }

    public start(): void {
        this.subscriptions = new Subscription();
        this.subscriptions.add(
            interval(3000)
                .pipe(switchMap(() => this._monitoringService.getMetrics('jvm.threads')))
                .subscribe({
                    next: (data) => this.buildThreadStatitic(data),
                    error: (err) => this._notifierService.error(err),
                })
        );
    }

    public stop(): void {
        if (this.subscriptions) {
            this.subscriptions.unsubscribe();
        }
    }

    private buildThreadStatitic(metrics: Metrics) {
        const threads: Threads = {
            count: metrics.getGaugeValue('jvm.threads.count'),
            runnable: {
                count: metrics.getGaugeValue('jvm.threads.runnable.count'),
            },
            waiting: {
                count: metrics.getGaugeValue('jvm.threads.waiting.count'),
            },
            blocked: {
                count: metrics.getGaugeValue('jvm.threads.blocked.count'),
            },
            free: {},
        };
        threads.free = {
            count:
                threads.count -
                ((threads.runnable.count ?? 0) + (threads.waiting.count ?? 0) + (threads.blocked.count ?? 0)),
        };
        threads.free.percentage = Math.round(((threads.free.count ?? 0) * 100) / threads.count);
        threads.runnable.percentage = Math.round(((threads.runnable.count ?? 0) * 100) / threads.count);
        threads.waiting.percentage = Math.round(((threads.waiting.count ?? 0) * 100) / threads.count);
        threads.blocked.percentage = Math.round(((threads.blocked.count ?? 0) * 100) / threads.count);
        this.threads = threads;
        this._cd.markForCheck();
    }
}
