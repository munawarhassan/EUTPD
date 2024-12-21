import { ChangeDetectionStrategy, Component, HostBinding, Input, OnInit, ViewChild } from '@angular/core';
import { ScrollDirective, ScrollOptions } from '@devacfr/bootstrap';
import { ClassBuilder } from '@devacfr/util';
import { MenuComponentType } from '../menu';

@Component({
    selector: 'lt-aside-menu',
    templateUrl: './aside-menu.component.html',
    styleUrls: ['./aside-menu.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AsideMenuComponent implements OnInit {
    @Input()
    public scrollOptions: ScrollOptions = {
        activate: { default: false, lg: true },
        height: 'auto',
        dependencies: '#lt_header,#lt_aside_footer',
        wrappers: '#lt_aside_menu',
    };

    @HostBinding('class')
    public get class(): string {
        const builder = ClassBuilder.create('aside-menu flex-column-fluid px-5');
        return builder.toString();
    }

    @Input()
    public menu: MenuComponentType | undefined;

    @ViewChild(ScrollDirective, { static: true })
    private scroll!: ScrollDirective;

    ngOnInit(): void {
        this.updateView();
    }

    public updateView(): void {
        setTimeout(() => this.scroll.update(), 50);
    }
}
