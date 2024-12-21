import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { InfoService, ScmRequest } from '@devacfr/core';
import { BsModalRef } from 'ngx-bootstrap/modal';

@Component({
    selector: 'app-about',
    templateUrl: './about-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AboutDialogComponent implements OnInit {
    public info: ScmRequest | undefined;

    constructor(public bsModalRef: BsModalRef, private _infoService: InfoService, private _cd: ChangeDetectorRef) {}

    ngOnInit() {
        this.refresh();
    }

    public close(): void {
        this.bsModalRef.hide();
    }

    private refresh() {
        this._infoService.getScmInfo().subscribe((info: ScmRequest) => {
            this.info = info;
            this._cd.markForCheck();
        });
    }
}
