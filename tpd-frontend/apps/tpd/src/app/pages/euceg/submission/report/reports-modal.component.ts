import { Component, OnDestroy } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { SubmissionReportService } from '@devacfr/euceg';
import { NotifierService } from '@devacfr/layout';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-reports-modal',
    templateUrl: './reports-modal.component.html',
})
export class ReportsModalComponent implements OnDestroy {
    public title: string | undefined;

    private _subscription = new Subscription();

    constructor(
        public svgIcons: SvgIcons,
        private _notifierService: NotifierService,
        private bsModalRef: BsModalRef,
        private _reportService: SubmissionReportService
    ) {}

    public ngOnDestroy(): void {
        this._subscription.unsubscribe();
    }

    public hide() {
        this.bsModalRef.hide();
    }
}
