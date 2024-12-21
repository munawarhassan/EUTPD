import { Component, OnInit, ViewChild } from '@angular/core';
import { BlockUI, SvgIcons } from '@devacfr/bootstrap';
import { Channels, GeneralInfo, InfoService } from '@devacfr/core';
import { AttachmentSendStatus, EucegStatisticService, ProductPirStatus, SubmissionStatus } from '@devacfr/euceg';
import { DaterangepickerType } from '@devacfr/forms';
import { getCSSVariableValue } from '@devacfr/util';
import { ApexChart, ApexDataLabels, ApexFill, ApexLegend, ApexNonAxisChartSeries, ChartComponent } from 'ng-apexcharts';
import { Observable } from 'rxjs';
import { finalize, tap } from 'rxjs/operators';

type PieChartOptions = {
    series: ApexNonAxisChartSeries;
    colors: string[];
    chart: ApexChart;
    fill: ApexFill;
    legend: ApexLegend;
    labels: string[];
    dataLabels: ApexDataLabels;
};

const defaultPieChartOptions: PieChartOptions = {
    series: [],
    chart: {
        width: '100%',
        type: 'pie',
    },
    fill: {
        opacity: 0.7,
    },
    colors: [],
    legend: {
        show: false,
    },
    dataLabels: {
        enabled: false,
    },
    labels: [],
};

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    // changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HomeComponent implements OnInit {
    public info$: Observable<GeneralInfo>;

    // all audit associated to euceg
    public channels = [Channels.EUCEG];
    // 'today' by default
    public defaultRangeActivities: DaterangepickerType = { startDate: new Date(), endDate: new Date() };

    private _blockCountSubmission = new BlockUI('m_card_submission_count');
    private _blockCountAttachment = new BlockUI('m_card_attachment_count');
    private _blockCountTobacco = new BlockUI('m_card_tobacco_count');
    private _blockCountEcig = new BlockUI('m_card_ecig_count');

    @ViewChild('submissionChart', { static: false })
    private submissionChart!: ChartComponent;
    public submissionChartOptions: PieChartOptions;

    @ViewChild('ecigChart', { static: false })
    private ecigChart!: ChartComponent;
    public ecigChartOptions: PieChartOptions;

    @ViewChild('tobaccoChart', { static: false })
    private tobaccoChart!: ChartComponent;
    public tobaccoChartOptions: PieChartOptions;

    @ViewChild('attachmentChart', { static: false })
    private attachmentChart!: ChartComponent;
    public attachmentOptions: PieChartOptions;

    private _colors = {
        gray: getCSSVariableValue('--bs-gray-600'),
        primary: getCSSVariableValue('--bs-primary'),
        info: getCSSVariableValue('--bs-info'),
        secondary: getCSSVariableValue('--bs-secondary'),
        success: getCSSVariableValue('--bs-success'),
        danger: getCSSVariableValue('--bs-danger'),
        warning: getCSSVariableValue('--bs-warning'),
    };

    constructor(
        public svgIcons: SvgIcons,
        private _statisticService: EucegStatisticService,
        private _infoService: InfoService
    ) {
        this.info$ = this._infoService.getInfo();
        this.submissionChartOptions = {
            ...defaultPieChartOptions,
        };
        this.ecigChartOptions = {
            ...defaultPieChartOptions,
        };
        this.tobaccoChartOptions = {
            ...defaultPieChartOptions,
        };
        this.attachmentOptions = {
            ...defaultPieChartOptions,
        };
    }

    public countSubmission$ = this._statisticService.countSubmissionByStatus().pipe(
        tap((result) => {
            const series: ApexNonAxisChartSeries = [];
            const labels: string[] = [];
            const colors: string[] = [];
            if (!result.partitions) {
                return;
            }
            for (const status in result.partitions) {
                if (Object.prototype.hasOwnProperty.call(result.partitions, status)) {
                    labels.push(status);
                    colors.push(this.getSubmissionStatusColor(status as SubmissionStatus));
                    series.push(result.partitions[status]);
                }
            }
            this.submissionChartOptions = { ...this.submissionChartOptions, series, colors, labels };
            this.submissionChart.updateOptions(this.submissionChartOptions);
        }),
        finalize(() => this._blockCountSubmission.release())
    );

    public countAttachment$ = this._statisticService.countAttachmentByStatus().pipe(
        tap((result) => {
            const series: ApexNonAxisChartSeries = [];
            const labels: string[] = [];
            const colors: string[] = [];
            if (!result.partitions) {
                return;
            }
            for (const status in result.partitions) {
                if (Object.prototype.hasOwnProperty.call(result.partitions, status)) {
                    labels.push(status);
                    colors.push(this.getAttachnentStatusColor(status as AttachmentSendStatus));
                    series.push(result.partitions[status]);
                }
            }
            this.attachmentOptions = { ...this.submissionChartOptions, series, colors, labels };
            this.attachmentChart.updateOptions(this.attachmentOptions);
        }),
        finalize(() => this._blockCountAttachment.release())
    );

    public countTobacco$ = this._statisticService.countProductByPirStatus('TOBACCO').pipe(
        tap((result) => {
            const series: ApexNonAxisChartSeries = [];
            const labels: string[] = [];
            const colors: string[] = [];
            if (!result.partitions) {
                return;
            }
            for (const status in result.partitions) {
                if (Object.prototype.hasOwnProperty.call(result.partitions, status)) {
                    labels.push(status);
                    colors.push(this.getPirStatusStatusColor(status as ProductPirStatus));
                    series.push(result.partitions[status]);
                }
            }
            this.tobaccoChartOptions = { ...this.tobaccoChartOptions, series, colors, labels };
            this.tobaccoChart.updateOptions(this.tobaccoChartOptions);
        }),
        finalize(() => this._blockCountTobacco.release())
    );

    public countEcig$ = this._statisticService.countProductByPirStatus('ECIGARETTE').pipe(
        tap((result) => {
            const series: ApexNonAxisChartSeries = [];
            const labels: string[] = [];
            const colors: string[] = [];
            if (!result.partitions) {
                return;
            }
            for (const status in result.partitions) {
                if (Object.prototype.hasOwnProperty.call(result.partitions, status)) {
                    labels.push(status);
                    colors.push(this.getPirStatusStatusColor(status as ProductPirStatus));
                    series.push(result.partitions[status]);
                }
            }
            this.ecigChartOptions = { ...this.ecigChartOptions, series, colors, labels };
            this.ecigChart.updateOptions(this.ecigChartOptions);
        }),
        finalize(() => this._blockCountEcig.release())
    );

    public ngOnInit(): void {
        this._blockCountSubmission.block();
        this._blockCountAttachment.block();
        this._blockCountTobacco.block();
        this._blockCountEcig.block();
    }

    public getSubmissionStatusColor(status: SubmissionStatus): string {
        switch (status) {
            case 'NOT_SEND':
            case 'PENDING':
                return this._colors.gray;
            case 'SUBMITTING':
                return this._colors.primary;
            case 'SUBMITTED':
                return this._colors.success;
            case 'ERROR':
                return this._colors.danger;
            case 'CANCELLED':
                return this._colors.warning;
        }
    }

    public getPirStatusStatusColor(status: ProductPirStatus): string {
        switch (status) {
            case 'AWAITING':
                return this._colors.gray;
            case 'ACTIVE':
                return this._colors.success;
            case 'INACTIVE':
                return this._colors.warning;
            case 'WITHDRAWN':
                return this._colors.primary;

            default:
                return this._colors.gray;
        }
    }

    public getAttachnentStatusColor(status: AttachmentSendStatus): string {
        switch (status) {
            case 'NO_SEND':
                return this._colors.gray;
            case 'SENDING':
                return this._colors.success;
            case 'SENT':
                return this._colors.success;
            default:
                return this._colors.gray;
        }
    }
}
