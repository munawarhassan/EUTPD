import { ChangeDetectorRef, Component, OnDestroy } from '@angular/core';
import { Metrics, MonitoringService } from '@devacfr/core';
import { NotifierService } from '@devacfr/layout';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';

interface Memory {
    min?: number;
    max?: number;
    committed?: number;
    used?: number;
    value?: any;
}

@Component({
    selector: 'app-metrics-memory',
    templateUrl: './metrics-memory.component.html',
})
export class MetricsMemoryComponent implements OnDestroy {
    public memory: Memory = {};
    public heap: Memory = {};
    public noheap: Memory = {};

    private subscriptions;

    constructor(
        private _cd: ChangeDetectorRef,
        private _monitoringService: MonitoringService,
        private _notifierService: NotifierService
    ) {}

    public ngOnDestroy(): void {
        this.stop();
    }

    public start(): void {
        this.subscriptions = new Subscription();
        // first
        this.subscriptions.add(
            interval(3000)
                .pipe(switchMap(() => this._monitoringService.getMetrics('jvm.memory')))
                .subscribe({
                    next: (metrics) => this.buildMemoryStatitic(metrics),
                    error: (err) => this._notifierService.error(err),
                })
        );
    }

    public stop(): void {
        if (this.subscriptions) {
            this.subscriptions.unsubscribe();
        }
    }

    private buildMemoryStatitic(metrics: Metrics) {
        this.memory = {
            min: 0,
            max: Math.round(metrics.getGaugeValue('jvm.memory.total.max') / 1000000),
            used: Math.round(metrics.getGaugeValue('jvm.memory.total.used') / 1000000),
        };
        this.memory.value = (((this.memory.used ?? 0) * 100) / (this.memory.max ?? 1)).toFixed(2);

        this.heap = {
            min: 0,
            max: Math.round(metrics.getGaugeValue('jvm.memory.heap.max') / 1000000),
            used: Math.round(metrics.getGaugeValue('jvm.memory.heap.used') / 1000000),
        };
        this.heap.value = (((this.heap.used ?? 0) * 100) / (this.heap.max ?? 1)).toFixed(2);

        this.noheap = {
            min: 0,
            committed: Math.round(metrics.getGaugeValue('jvm.memory.non-heap.committed') / 1000000),
            used: Math.round(metrics.getGaugeValue('jvm.memory.non-heap.used') / 1000000),
        };
        this.noheap.value = (((this.noheap.used ?? 0) * 100) / (this.noheap.committed ?? 1)).toFixed(2);
        this._cd.markForCheck();
    }
}
