import { Component } from '@angular/core';
import { EucegService } from '@devacfr/euceg';
import _ from 'lodash-es';
import { BsModalRef } from 'ngx-bootstrap/modal';

@Component({
    selector: 'app-affiliate-modal',
    templateUrl: './affiliate-modal.component.html',
})
export class AffiliateModalComponent {
    public onClose: () => void = _.noop;

    public onCancel: () => void = _.noop;

    public affiliate: any;

    public readonly = false;

    constructor(public bsModalRef: BsModalRef, public euceg: EucegService) {}

    public close(): void {
        this.bsModalRef.hide();
        this.onClose();
    }

    public cancel(): void {
        this.bsModalRef.hide();
        this.onCancel();
    }
}
