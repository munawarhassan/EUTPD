import { Component, HostBinding, Input } from '@angular/core';
import { BsColor } from '@devacfr/util';

@Component({
    selector: 'app-timeline-item',
    templateUrl: 'timeline-item.component.html',
})
export class TimelineItemComponent {
    @HostBinding()
    public class = 'timeline-item';

    @Input()
    public icon: string | undefined;

    @Input()
    public color: BsColor = 'primary';

    @Input()
    public state: string | undefined;

    @Input()
    public audit:
        | {
              timestamp: number;
              principal: string;
          }
        | undefined;
}
