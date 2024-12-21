import { Directive, HostBinding, Input, OnDestroy } from '@angular/core';
import { LayoutService } from '../../service';
import { ClassBuilder } from '@devacfr/util';
import { Subscription } from 'rxjs';

@Directive({ selector: 'lt-aside-footer,[ltAsideFooter]' })
export class AsideFooterDirective implements OnDestroy {
    @HostBinding('id')
    public id = 'lt_aside_footer';

    @HostBinding('class')
    @Input()
    public get class(): string {
        const builder = ClassBuilder.create('aside-footer flex-column-auto pt-5 pb-7 px-5');
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
