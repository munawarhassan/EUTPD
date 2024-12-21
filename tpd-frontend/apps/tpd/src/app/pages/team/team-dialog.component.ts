import { ChangeDetectionStrategy, Component } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';

@Component({
    selector: 'app-team',
    templateUrl: './team-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TeamDialogComponent {
    constructor(public bsModalRef: BsModalRef) {}

    public close(): void {
        this.bsModalRef.hide();
    }
}
