import { Directive, HostBinding, Input, TemplateRef } from '@angular/core';
import { ClassBuilder, getUniqueIdWithPrefix } from '@devacfr/util';
import { RootTab, TabPanel } from '.';

@Directive({
    // eslint-disable-next-line @angular-eslint/directive-selector
    selector: 'tab-pane, [tab-pane]',
    exportAs: 'tabPane',
})
export class TabPaneDirective implements TabPanel {
    @HostBinding('class')
    public get cssClass(): string {
        const builder = ClassBuilder.create('tab-pane');
        if (this.fade) builder.css('fade');
        if (this.active) builder.css('show active');
        return builder.toString();
    }

    @HostBinding('id')
    public id: string;

    @Input()
    public heading: string | undefined;

    @Input()
    public icon: string | undefined;

    @Input()
    public iconClass = '';

    @Input()
    public fade = true;

    @Input()
    public active = false;
    @Input()
    public parent: RootTab | undefined;

    constructor() {
        this.id = getUniqueIdWithPrefix('tab-pane');
    }

    public get useSVG() {
        return this.icon && this.icon.endsWith('.svg');
    }
}
