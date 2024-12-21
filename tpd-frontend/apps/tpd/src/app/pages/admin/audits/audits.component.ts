import { ChangeDetectionStrategy, Component } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { Channels } from '@devacfr/core';
import { DaterangepickerType } from '@devacfr/forms';

@Component({
    selector: 'app-audits',
    templateUrl: './audits.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuditsComponent {
    public range: DaterangepickerType = { startDate: new Date(), endDate: new Date() };

    public channels = [Channels.ADMIN_LOG, Channels.AUTHENTICATION];

    constructor(public svgIcons: SvgIcons) {}
}
