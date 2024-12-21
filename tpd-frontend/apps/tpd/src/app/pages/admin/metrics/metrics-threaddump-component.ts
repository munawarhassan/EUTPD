import { ChangeDetectorRef, Component, OnDestroy } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { MonitoringService, ThreadDump, ThreadDumps } from '@devacfr/core';
import { NotifierService } from '@devacfr/layout';
import { ClassBuilder } from '@devacfr/util';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-metrics-threaddump',
    templateUrl: './metrics-threaddump.component.html',
})
export class MetricsThreadDumpComponent implements OnDestroy {
    public threadDumps: ThreadDump[] | undefined;

    public threadDumpFilter = '';

    public threadDumpRunnable = 0;
    public threadDumpWaiting = 0;
    public threadDumpTimedWaiting = 0;
    public threadDumpBlocked = 0;
    public threadDumpAll = 0;

    public show = false;

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
            this._monitoringService.threadDump().subscribe({
                next: (threadDumps) => this.buildStatitic(threadDumps),
                error: (err) => this._notifierService.error(err),
            })
        );
    }

    public stop(): void {
        if (this.subscriptions) {
            this.subscriptions.unsubscribe();
        }
    }

    public buildStatitic(threadDumps: ThreadDumps) {
        this.threadDumps = threadDumps.threads;
        this.threadDumps?.forEach((threadDump) => {
            if (threadDump.threadState === 'RUNNABLE') {
                this.threadDumpRunnable += 1;
            } else if (threadDump.threadState === 'WAITING') {
                this.threadDumpWaiting += 1;
            } else if (threadDump.threadState === 'TIMED_WAITING') {
                this.threadDumpTimedWaiting += 1;
            } else if (threadDump.threadState === 'BLOCKED') {
                this.threadDumpBlocked += 1;
            }
        });

        this.threadDumpAll =
            this.threadDumpRunnable + this.threadDumpWaiting + this.threadDumpTimedWaiting + this.threadDumpBlocked;
        this._cd.markForCheck();
    }

    public getHeaderAccordionClass(threadState): string {
        const builder = ClassBuilder.create('ps-4 py-3');
        if (threadState === 'RUNNABLE') {
            builder.css('text-light bg-success');
        } else if (threadState === 'WAITING') {
            builder.css('text-light bg-primary');
        } else if (threadState === 'TIMED_WAITING') {
            builder.css('bg-light text-dark');
        } else if (threadState === 'BLOCKED') {
            builder.css('text-light bg-danger');
        } else {
            builder.css('text-light bg-info');
        }

        return builder.toString();
    }
}
