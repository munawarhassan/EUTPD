import { ChangeDetectionStrategy, Component, HostBinding, Input } from '@angular/core';
import { Auditable } from '@devacfr/core';

@Component({
    selector: 'app-audit-detail',
    templateUrl: './audit-detail.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuditDetailComponent {
    public AVATAR_VERSION = new Date().getTime();

    @HostBinding('class')
    public class = 'd-block mt-3 fs-7';

    @Input()
    public audit: Auditable | undefined;
}
