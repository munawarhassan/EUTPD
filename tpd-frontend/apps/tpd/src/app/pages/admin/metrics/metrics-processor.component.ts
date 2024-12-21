import { ChangeDetectorRef, Component, OnDestroy } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { Metrics, MonitoringService } from '@devacfr/core';
import { NotifierService } from '@devacfr/layout';
import { colorDarken } from '@devacfr/util';
import { ChartConfiguration } from 'chart.js';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Component({
    selector: 'app-metrics-processor',
    templateUrl: './metrics-processor.component.html',
})
export class MetricsProcessorComponent implements OnDestroy {
    public data: ChartConfiguration['data'];
    public options: ChartConfiguration['options'];
    private bufferSize = 100;
    private subscriptions;

    constructor(
        public svgIcons: SvgIcons,
        private _cd: ChangeDetectorRef,
        private _monitoringService: MonitoringService,
        private _notifierService: NotifierService
    ) {
        const series = Metrics.series<any>(
            this.bufferSize,
            [
                {
                    borderColor: '#00c5dc',
                    backgroundColor: '#eeeeee',
                    borderWidth: 2,
                    lineTension: 0,
                    pointRadius: 0,
                    fill: 'origin',
                },
                {
                    borderColor: '#36a3f7',
                    backgroundColor: colorDarken('#eeeeee', 0.5),
                    borderWidth: 2,
                    lineTension: 0,
                    pointRadius: 0,
                    fill: 'origin',
                },
            ],
            'JVM CPU Usage',
            'Machine CPU Usage'
        );

        this.data = {
            datasets: series as any,
            labels: new Array(this.bufferSize).fill(''),
        };
        this.options = {
            responsive: true,
            maintainAspectRatio: false,
            animation: false,
            events: [],
            scales: {
                yAxes: {
                    ticks: {
                        suggestedMin: 0,
                        suggestedMax: 50,
                    },
                },
            } as any,
        };
    }

    public ngOnDestroy(): void {
        this.stop();
    }

    public start(): void {
        this.subscriptions = new Subscription();
        this.subscriptions.add(
            interval(800)
                .pipe(switchMap(() => this._monitoringService.getMetrics('os')))
                .subscribe({
                    next: (data) => this.buildProcessorStatitic(new Metrics(data)),
                    error: (err) => this._notifierService.error(err),
                })
        );
    }

    public stop(): void {
        if (this.subscriptions) {
            this.subscriptions.unsubscribe();
        }
    }

    private buildProcessorStatitic(metrics: Metrics) {
        // copy array to enforce update of chart.
        if (!this.data.labels) {
            return;
        }
        const labels = this.data.labels.slice();
        const dataA = this.data.datasets[0].data.slice();
        const dataB = this.data.datasets[1].data.slice();

        labels.push(new Date().toLocaleTimeString());
        dataA.push(Metrics.percentage(metrics.getGaugeValue('os.process.cpu.load') * 100));
        dataB.push(Metrics.percentage(metrics.getGaugeValue('os.system.cpu.load') * 100));
        if (labels.length > this.bufferSize) {
            Metrics.splice(labels, labels.length - this.bufferSize);
            Metrics.splice(dataA, dataA.length - this.bufferSize);
            Metrics.splice(dataB, dataB.length - this.bufferSize);
        }
        this.data.labels = labels;
        this.data.datasets[0].data = dataA;
        this.data.datasets[1].data = dataB;
        this._cd.markForCheck();
    }
}
