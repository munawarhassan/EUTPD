import {
    AfterContentInit,
    ChangeDetectorRef,
    Component,
    ContentChildren,
    HostBinding,
    Inject,
    Input,
    QueryList,
} from '@angular/core';
import { ClassBuilder } from '@devacfr/util';
import { uniqueId } from 'lodash-es';
import { AccordionItemComponent } from './accordion-item.component';
import { AccordionConfig, ACCORDION_CONFIG } from './accordion.config';

type ModeType = 'icon-toggle' | 'borderless';

@Component({
    selector: 'lt-accordion',
    template: '<ng-content select="lt-accordion-item"></ng-content>',
})
export class AccordionComponent implements AfterContentInit {
    @HostBinding('attr.role')
    public role = 'tablist';

    @Input()
    public mode: ModeType[] | ModeType = [];

    @Input()
    public togglePosition: 'right' | 'left' = 'left';

    @Input()
    @HostBinding('attr.aria-multiselectable')
    public closeOthers: boolean | undefined;

    @HostBinding('id')
    public id: string;

    @ContentChildren(AccordionItemComponent)
    public accordionItems: QueryList<AccordionItemComponent> | undefined;

    private _class: string | undefined;

    @Input()
    @HostBinding('class')
    public get class(): string {
        const builder = ClassBuilder.create('d-flex flex-column accordion');
        if (this._class) {
            builder.css(this._class);
        }
        builder.flag('accordion-', ...this.mode);
        return builder.toString();
    }
    public set class(value: string) {
        this._class = value;
    }

    constructor(@Inject(ACCORDION_CONFIG) private _config: AccordionConfig, private _cd: ChangeDetectorRef) {
        Object.assign(this, this._config);
        this.id = uniqueId('ltAccordion');
    }

    public ngAfterContentInit(): void {
        this.accordionItems?.forEach((item) => (item.parent = this));
    }

    public get toggleSvg(): boolean {
        return this.mode != null && this.mode.indexOf('icon-toggle') >= 0;
    }
}
