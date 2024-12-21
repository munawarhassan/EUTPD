import { Directive, HostBinding, Input, OnDestroy } from '@angular/core';
import { ClassBuilder } from '@devacfr/util';
import { Subscription } from 'rxjs';
import { LayoutService } from '../../service';

@Directive({ selector: 'lt-footer,[ltFooter]' })
export class FooterDirective implements OnDestroy {
    @HostBinding('id')
    public id = 'lt_footer';

    @HostBinding('class')
    @Input()
    public get class(): string {
        const builder = ClassBuilder.create('footer py-4 d-flex flex-stack flex-row');
        if (this._class) builder.css(this._class);
        return builder.toString();
    }
    public set class(cl: string) {
        this._class = cl;
    }

    private _subscriptions = new Subscription();
    private _class: string | undefined;

    constructor(private _layout: LayoutService) {}

    ngOnDestroy() {
        this._subscriptions.unsubscribe();
    }
}
