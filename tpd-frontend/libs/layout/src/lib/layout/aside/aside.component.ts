import { ChangeDetectionStrategy, ChangeDetectorRef, Component, HostBinding, OnDestroy, OnInit } from '@angular/core';
import { ClassBuilder } from '@devacfr/util';
import { Subscription } from 'rxjs';
import { LayoutConfig } from '../../layout-config';
import { LayoutService } from '../../service';

@Component({
    selector: 'lt-aside',
    templateUrl: './aside.component.html',
    styleUrls: ['./aside.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AsideComponent implements OnInit, OnDestroy {
    @HostBinding('id')
    public id = 'lt_aside';

    @HostBinding('class')
    public get class(): string {
        const builder = ClassBuilder.create('aside');
        if (this._asideTheme) builder.css(`aside-${this._asideTheme}`);
        if (this._hoverable) builder.css(`aside-hoverable`);
        return builder.toString();
    }

    private _asideTheme = '';
    private _hoverable = false;

    private _subscriptions = new Subscription();

    constructor(private _layout: LayoutService, private _cd: ChangeDetectorRef) {
        this._subscriptions.add(this._layout.configUpdated().subscribe(this.init.bind(this)));
    }
    ngOnInit(): void {
        document.body.classList.add('aside-enabled');
    }

    ngOnDestroy() {
        this._subscriptions.unsubscribe();
        document.body.classList.remove('aside-enabled', 'aside-fixed');
    }

    private init(config: LayoutConfig) {
        if (config.aside.fixed) {
            document.body.classList.add('aside-fixed');
        }

        // if (config.aside.minimized) {
        //     document.body.setAttribute('data-kt-aside-minimize', 'on');
        // }
        this._hoverable = config.aside.hoverable;
        this._cd.markForCheck();
    }
}
