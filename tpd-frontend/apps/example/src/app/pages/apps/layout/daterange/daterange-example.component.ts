import { Component, OnInit } from '@angular/core';
import { ChangedValueEvent, DaterangepickerType } from '@devacfr/forms';
import moment from 'moment';

@Component({
    selector: 'app-daterange-example',
    templateUrl: 'daterange-example.component.html',
})
export class DateRangeExampleComponent implements OnInit {
    public input_date: Date | null = null;

    public string_date: string | null = null;

    public range_date: Date[] | null = null;

    public range_string_date: string[] | null = null;

    public title = '';
    public rangeText = '';
    public buttonRange: DaterangepickerType = { startDate: new Date(), endDate: new Date() };

    ngOnInit(): void {
        this.adaptButton({ range: this.buttonRange });
    }

    public updateStringDate(value: Date[]) {
        if (value) this.range_string_date = value.map((d) => d.toISOString());
        else this.range_string_date = [];
    }

    public adaptButton(event: ChangedValueEvent): void {
        let title = event?.range.label || '';
        let rangeText = '';
        const range = event.range;
        const endDate = moment(range.endDate);
        const startDate = moment(range.startDate);

        if (endDate.unix() - startDate.unix() < 100 || event?.range.label === 'Today') {
            title = 'Today:';
            rangeText = startDate.format('MMM D');
        } else if (event.range.label === 'Yesterday') {
            title = 'Yesterday:';
            rangeText = startDate.format('MMM D');
        } else {
            rangeText = startDate.format('MMM D') + ' - ' + endDate.format('MMM D');
        }

        this.rangeText = rangeText;
        this.title = title;
    }
}
