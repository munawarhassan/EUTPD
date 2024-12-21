import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { BlockUI } from '@devacfr/bootstrap';
import { EucegStatisticService, HistogramInterval, HistogramRequest, HistogramResult } from '@devacfr/euceg';
import { NotifierService } from '@devacfr/layout';
import { getCSS, getCSSVariableValue } from '@devacfr/util';
import {
    ApexAxisChartSeries,
    ApexChart,
    ApexDataLabels,
    ApexFill,
    ApexGrid,
    ApexLegend,
    ApexPlotOptions,
    ApexStates,
    ApexStroke,
    ApexTooltip,
    ApexXAxis,
    ApexYAxis,
    ChartComponent,
} from 'ng-apexcharts';
import { EMPTY, forkJoin, Observable, of, ReplaySubject } from 'rxjs';
import { catchError, finalize, map, switchMap, tap } from 'rxjs/operators';

type ChartOptions = {
    series: ApexAxisChartSeries;
    chart: ApexChart;
    stroke: ApexStroke;
    xaxis: ApexXAxis;
    yaxis: ApexYAxis;
    fill: ApexFill;
    states: ApexStates;
    tooltip: ApexTooltip;
    plotOptions: ApexPlotOptions;
    legend: ApexLegend;
    grid: ApexGrid;
    dataLabels: ApexDataLabels;
};

@Component({
    selector: 'app-chart-recent',
    templateUrl: 'chart-recent.component.html',
})
export class ChartRecentComponent implements OnInit {
    public intervals: { name: string; value: string }[] = [
        // disable day, elasticsearch doesn't work correctly
        {
            name: 'Day',
            value: 'day',
        },
        {
            name: 'Week',
            value: 'week',
        },
        {
            name: 'Month',
            value: 'month',
        },
        {
            name: 'Year',
            value: 'year',
        },
    ];

    public histogramCreated$: Observable<ChartOptions>;

    public formControl: FormGroup;

    private _selectedInterval$: ReplaySubject<HistogramRequest> = new ReplaySubject<HistogramRequest>();
    private _selectedInterval: HistogramRequest = { interval: 'day', bounds: 7 };

    private _block = new BlockUI('m_card_recent_chart');

    @ViewChild(ChartComponent, { static: false })
    private chart!: ChartComponent;
    private _chartOptions!: ChartOptions;

    constructor(
        private _element: ElementRef<HTMLElement>,
        private _fb: FormBuilder,
        private _statisticService: EucegStatisticService,
        private _notifierService: NotifierService
    ) {
        this.formControl = this._fb.group({
            interval: [null],
        });
        this.formControl.patchValue(this._selectedInterval);
        this.formControl.get('interval')?.valueChanges.subscribe((value: HistogramInterval) => {
            let bounds = 6;
            switch (value) {
                case 'year':
                    bounds = 11;
                    break;
                default:
                    break;
            }
            this.selectedInterval = {
                interval: value,
                bounds: bounds,
            };
        });

        this.histogramCreated$ = this._selectedInterval$.pipe(
            tap(() => this._block.block()),
            switchMap((request) =>
                forkJoin([
                    this._statisticService.getHistogramRecentSubmission(request),
                    this._statisticService.getHistogramRecentEcigProduct(request),
                    this._statisticService.getHistogramRecentTobaccoProduct(request),
                ]).pipe(
                    map(([submissionResult, ecigResult, tobaccoResult]) => {
                        const options = this.updateChartOptions(submissionResult, ecigResult, tobaccoResult);
                        if (this.chart) {
                            this.chart.updateOptions(options);
                        }
                        return options;
                    }),
                    catchError((err) => {
                        this._notifierService.error(err);
                        return EMPTY;
                    }),
                    finalize(() => this._block.release())
                )
            )
        );
    }

    ngOnInit(): void {
        this._chartOptions = this.initCharts();
        this.selectedInterval = this._selectedInterval;
    }

    public set selectedInterval(value: HistogramRequest) {
        this._selectedInterval = value;
        this._selectedInterval$.next(value);
    }

    private initCharts(): ChartOptions {
        const element = this._element.nativeElement.querySelector('#m_histogram_recent') as HTMLElement;

        const height = parseInt(getCSS(element, 'height'));
        const labelColor = getCSSVariableValue('--bs-gray-500');
        const borderColor = getCSSVariableValue('--bs-gray-200');

        const options: ChartOptions = {
            series: [],
            chart: {
                fontFamily: 'inherit',
                type: 'bar',
                height: height,
                toolbar: {
                    show: false,
                },
            },
            plotOptions: {
                bar: {
                    horizontal: false,
                    // columnWidth: '70%',
                    borderRadius: 4,
                },
            },
            legend: {
                show: false,
            },
            dataLabels: {
                enabled: false,
            },
            stroke: {
                show: true,
                width: 2,
                colors: ['transparent'],
            },
            xaxis: {
                axisBorder: {
                    show: false,
                },
                axisTicks: {
                    show: false,
                },
                labels: {
                    style: {
                        colors: labelColor,
                        fontSize: '12px',
                    },
                },
            },
            yaxis: {
                labels: {
                    style: {
                        colors: labelColor,
                        fontSize: '12px',
                    },
                },
            },
            fill: {
                opacity: 1,
            },
            states: {
                normal: {
                    filter: {
                        type: 'none',
                        value: 0,
                    },
                },
                hover: {
                    filter: {
                        type: 'none',
                        value: 0,
                    },
                },
                active: {
                    allowMultipleDataPointsSelection: false,
                    filter: {
                        type: 'none',
                        value: 0,
                    },
                },
            },
            tooltip: {
                style: {
                    fontSize: '12px',
                },
            },
            grid: {
                borderColor: borderColor,
                strokeDashArray: 4,
                yaxis: {
                    lines: {
                        show: true,
                    },
                },
            },
        };
        return options;
    }

    private updateChartOptions(
        submissionResult: HistogramResult,
        ecigResult: HistogramResult,
        tobaccoResult: HistogramResult
    ): ChartOptions {
        const primaryColor = getCSSVariableValue('--bs-primary');
        const darkColor = getCSSVariableValue('--bs-dark');
        const grayColor = getCSSVariableValue('--bs-gray-600');

        const defaultOptions = this._chartOptions;
        const options = {
            ...defaultOptions,
            series: [
                {
                    name: 'Submission',
                    data: submissionResult.data.submission,
                    color: primaryColor,
                },
                {
                    name: 'Tobacco Product',
                    data: tobaccoResult.data.tobacco_product,
                    color: darkColor,
                },
                {
                    name: 'Ecig Product',
                    data: ecigResult.data.ecig_product,
                    color: grayColor,
                },
            ],
        };
        options.xaxis.categories = submissionResult.series;
        return options;
    }
}
