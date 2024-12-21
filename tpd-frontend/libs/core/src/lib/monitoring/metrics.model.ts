import { Timers } from '.';

export class Metrics {
    public timers?: Timers[];
    public gauges?: Record<string, unknown>[];

    public static splice(ar: unknown[], length): unknown[] | undefined {
        if (length > 0) {
            return ar.splice(0, length);
        }
        return undefined;
    }

    public static percentage(value) {
        if (value === 'Nan' || isNaN(value)) {
            return 0;
        }
        if (value > 100) {
            value = 100;
        }
        return value;
    }

    /**
     * create an empty datasource series.
     *
     * @param size size of serie.
     * @param series name of first series
     * @param seriesOpt list of series name.
     */
    public static series<TOption = Record<string, unknown>>(
        size: number,
        options: TOption[],
        series: string,
        ...seriesOpt: string[]
    ): { label: string; data: number[] } | TOption[] {
        const data: { label: string; data: number[] } | TOption[] = [];
        const ar = [series, ...seriesOpt];
        ar.forEach((val, index: number) =>
            data.push({
                data: new Array(size).fill(null),
                label: val,
                ...options[index],
            })
        );
        return data;
    }

    constructor(metrics: any) {
        Object.assign(this, metrics);
    }

    public getTimers(this: Metrics, key: string): Timers | undefined {
        if (this.timers) {
            return this.timers[key];
        }
        return undefined;
    }

    public getGaugeValue(this: Metrics, key: string): number {
        if (this.gauges) {
            const gauge = this.gauges[key];
            if (gauge) {
                return gauge.value;
            }
        }
        return 0;
    }
}
