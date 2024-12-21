import { TableOptions } from '@devacfr/layout';

export class ReportList {
    public static tableOptions: TableOptions = {
        columns: [
            {
                name: 'name',
                sort: false,
                i18n: 'submissions.report.fields.filename',
            },
            {
                name: 'username',
                sort: false,
                i18n: 'submissions.report.fields.username',
            },
            {
                name: 'modified',
                sort: false,
                i18n: 'submissions.report.fields.modified',
            },
            {
                name: 'type',
                sort: false,
                i18n: 'submissions.report.fields.type',
            },
            {
                name: 'action',
                sort: false,
                i18n: 'submissions.report.fields.action',
                align: 'center',
            },
        ],
    };

    constructor(
        public id: string,
        public name: string,
        public size: number,
        public username: string,
        public modified: Date,
        public type: string,
        public url: string
    ) {}
}
