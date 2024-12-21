import { ChangeDetectorRef, Component, OnDestroy } from '@angular/core';
import { Metrics, MonitoringService } from '@devacfr/core';
import { NotifierService } from '@devacfr/layout';
import { colorDarken } from '@devacfr/util';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { colorSets } from './metrics-helper';

@Component({
    selector: 'app-metrics-network',
    templateUrl: './metrics-network.component.html',
})
export class MetricsNetworkComponent implements OnDestroy {
    public chartRequestRate;
    public colorScheme: any;
    public chartRequest;

    private bufferSize = 80;
    private intervalTime = 1000;
    private subscriptions;

    constructor(
        private _cd: ChangeDetectorRef,
        private _monitoringService: MonitoringService,
        private _notifierService: NotifierService
    ) {
        this.colorScheme = colorSets.find((c) => c.name === 'cool');

        this.chartRequest = {
            data: Metrics.series(
                this.bufferSize,
                [
                    {
                        borderColor: this.colorScheme.domain[0],
                        backgroundColor: '#eeeeee',
                        borderWidth: 2,
                        lineTension: 0,
                        pointRadius: 0,
                        fill: 'origin',
                    },
                    {
                        borderColor: this.colorScheme.domain[1],
                        backgroundColor: colorDarken('#eeeeee', 2),
                        borderWidth: 2,
                        lineTension: 0,
                        pointRadius: 0,
                        fill: 'origin',
                    },
                    {
                        borderColor: this.colorScheme.domain[2],
                        backgroundColor: colorDarken('#eeeeee', 4),
                        borderWidth: 2,
                        lineTension: 0,
                        pointRadius: 0,
                        fill: 'origin',
                    },
                    {
                        borderColor: this.colorScheme.domain[3],
                        backgroundColor: colorDarken('#eeeeee', 6),
                        borderWidth: 2,
                        lineTension: 0,
                        pointRadius: 0,
                        fill: 'origin',
                    },
                ],
                'percentile 50% (seconds)',
                'percentile 75% (seconds)',
                'percentile 95% (seconds)',
                'percentile 99.9% (seconds)'
            ),
            labels: new Array(this.bufferSize).fill(''),
            options: {
                tooltips: {
                    enabled: false,
                },
                responsive: true,
                maintainAspectRatio: false,
                animation: false,
                events: [],
                scales: {
                    yAxes: {
                        ticks: {
                            suggestedMin: 0,
                        },
                    },
                },
            },
        };

        this.chartRequestRate = {
            data: Metrics.series(
                this.bufferSize,
                [
                    {
                        borderColor: this.colorScheme.domain[0],
                        backgroundColor: '#eeeeee',
                        borderWidth: 2,
                        lineTension: 0,
                        pointRadius: 0,
                        fill: 'origin',
                    },
                    {
                        borderColor: this.colorScheme.domain[1],
                        backgroundColor: colorDarken('#eeeeee', 2),
                        borderWidth: 2,
                        lineTension: 0,
                        pointRadius: 0,
                        fill: 'origin',
                    },
                    {
                        borderColor: this.colorScheme.domain[2],
                        backgroundColor: colorDarken('#eeeeee', 4),
                        borderWidth: 2,
                        lineTension: 0,
                        pointRadius: 0,
                        fill: 'origin',
                    },
                    {
                        borderColor: this.colorScheme.domain[3],
                        backgroundColor: colorDarken('#eeeeee', 6),
                        borderWidth: 2,
                        lineTension: 0,
                        pointRadius: 0,
                        fill: 'origin',
                    },
                ],
                'm1 (call/second)',
                'm5 (call/second)',
                'm15 (call/second)'
            ),
            labels: new Array(this.bufferSize).fill(''),
            options: {
                responsive: true,
                maintainAspectRatio: false,
                animation: false,
                events: [],
                scales: {
                    yAxes: {
                        ticks: {
                            suggestedMin: 0,
                        },
                    },
                },
            },
            colors: [
                {
                    borderColor: this.colorScheme.domain[0],
                    borderWidth: 2,
                    lineTension: 0,
                    pointRadius: 0,
                },
                {
                    borderColor: this.colorScheme.domain[1],
                    borderWidth: 2,
                    lineTension: 0,
                    pointRadius: 0,
                },
                {
                    borderColor: this.colorScheme.domain[2],
                    borderWidth: 2,
                    lineTension: 0,
                    pointRadius: 0,
                },
            ],
        };
    }

    public ngOnDestroy(): void {
        this.stop();
    }

    public start(): void {
        this.subscriptions = new Subscription();
        this.subscriptions.add(
            interval(this.intervalTime)
                .pipe(switchMap(() => this._monitoringService.getMetrics('InstrumentedRequest')))
                .subscribe({
                    next: (metrics) => {
                        this.buildRequestRateStatitic(metrics);
                        this.buildRequestStatitic(metrics);
                        this._cd.markForCheck();
                    },
                    error: (err) => this._notifierService.error(err),
                })
        );
    }

    public stop(): void {
        if (this.subscriptions) {
            this.subscriptions.unsubscribe();
        }
    }

    private buildRequestStatitic(metrics: Metrics) {
        const timers = metrics.getTimers('InstrumentedRequest.requests');
        if (timers == null) {
            return;
        }

        const p50 = this.chartRequest.data[0].data.slice();
        const p75 = this.chartRequest.data[1].data.slice();
        const p95 = this.chartRequest.data[2].data.slice();
        const p999 = this.chartRequest.data[3].data.slice();
        const labels = this.chartRequest.labels.slice();

        labels.push(new Date().toLocaleTimeString());
        p50.push(timers.p50);
        p75.push(timers.p75);
        p95.push(timers.p95);
        p999.push(timers.p999);

        if (p50.length > this.bufferSize) {
            Metrics.splice(labels, labels.length - this.bufferSize);
            Metrics.splice(p50, p50.length - this.bufferSize);
            Metrics.splice(p75, p75.length - this.bufferSize);
            Metrics.splice(p95, p95.length - this.bufferSize);
            Metrics.splice(p999, p999.length - this.bufferSize);
        }

        this.chartRequest.labels = labels;
        this.chartRequest.data[0].data = p50;
        this.chartRequest.data[1].data = p75;
        this.chartRequest.data[2].data = p95;
        this.chartRequest.data[3].data = p999;

        /*
        this.refLinesRequest = [
          { value: timers.max, name: 'Maximum' },
          { value: timers.mean, name: 'Average' },
          { value: timers.min, name: 'Minimum' }
        ];
        */
    }

    private buildRequestRateStatitic(metrics: Metrics) {
        const timers = metrics.getTimers('InstrumentedRequest.requests');
        if (timers == null) {
            return;
        }

        const m1 = this.chartRequestRate.data[0].data.slice();
        const m5 = this.chartRequestRate.data[1].data.slice();
        const m15 = this.chartRequestRate.data[2].data.slice();
        const labels = this.chartRequest.labels.slice();

        labels.push(new Date().toLocaleTimeString());
        m1.push(timers.m1_rate);
        m5.push(timers.m5_rate);
        m15.push(timers.m15_rate);

        if (m1.length > this.bufferSize) {
            Metrics.splice(labels, labels.length - this.bufferSize);
            Metrics.splice(m1, m1.length - this.bufferSize);
            Metrics.splice(m5, m5.length - this.bufferSize);
            Metrics.splice(m15, m15.length - this.bufferSize);
        }
        this.chartRequestRate.labels = labels;
        this.chartRequestRate.data[0].data = m1;
        this.chartRequestRate.data[1].data = m5;
        this.chartRequestRate.data[2].data = m15;

        /*
        this.refLinesRequestRate = [
          { value: timers.mean_rate, name: 'Average' }
        ];
        */
    }
}
