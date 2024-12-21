import { Component, ElementRef, Input, OnChanges, SimpleChanges, ViewEncapsulation } from '@angular/core';
import * as Diff2Html from 'diff2html';
import { OutputFormatType } from 'diff2html/lib/types';

const defaultOptions: Diff2Html.Diff2HtmlConfig = {
    drawFileList: true,
    matching: 'lines',
};

@Component({
    selector: 'app-diff-html',
    template: ` <div [innerHTML]="outputHtml"></div> `,
    encapsulation: ViewEncapsulation.None,
    styleUrls: ['./diff-html.component.scss'],
})
export class DiffHtmlComponent implements OnChanges {
    @Input()
    public outputFormat: OutputFormatType = 'line-by-line';

    @Input()
    public fileSummary = true;

    @Input()
    public get diff(): string | undefined {
        return this._diffSource;
    }

    public set diff(v: string | undefined) {
        this._diffSource = v ? v : '';
        this.outputHtml = Diff2Html.html(this._diffSource, this.options);
    }

    @Input()
    public options: Diff2Html.Diff2HtmlConfig = defaultOptions;

    public outputHtml: string | undefined;

    private _diffSource: string | undefined;

    constructor(private _elementRef: ElementRef) {}

    ngOnChanges(changes: SimpleChanges): void {
        let changed = false;
        if (changes.outputFormat) {
            this.options.outputFormat = this.outputFormat;
            changed = true;
        }
        if (changes.fileSummary) {
            this.options.drawFileList =
                changes.fileSummary.currentValue === 'true' || changes.fileSummary.currentValue === '';
            changed = true;
        }
        if (changes.options) {
            changed = true;
        }
        if (changed && this._diffSource) {
            this.outputHtml = Diff2Html.html(this._diffSource, this.options);
        }
    }
}
