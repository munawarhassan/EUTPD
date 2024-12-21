import { Component, HostBinding, Input, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { LayoutService } from '../../service';
@Component({
    selector: 'lt-content',
    templateUrl: './content.component.html',
    styleUrls: ['./content.component.scss'],
})
export class ContentComponent implements OnDestroy {
    @HostBinding('id')
    public id = 'lt_content';

    @Input()
    public width: 'fluid' | 'fixed' = 'fixed';

    private _subscriptions = new Subscription();

    constructor(private _layout: LayoutService) {
        this.width = this._layout.config.content.width ?? 'fixed';
    }

    ngOnDestroy() {
        this._subscriptions.unsubscribe();
    }
}
