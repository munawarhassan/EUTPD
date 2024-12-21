import { Component, HostBinding, Input } from '@angular/core';
import { BsColor, ClassBuilder, Progress } from '@devacfr/util';

@Component({
    selector: 'lt-progress-bar-item',
    template: '<ng-content></ng-content>',
})
export class ProgressBarItemComponent {
    @HostBinding('style.width')
    public get width(): string {
        return this.progress.percentage + '%';
    }

    @Input()
    public progress: Progress = new Progress();

    @Input()
    public color: BsColor | undefined;

    @Input()
    public striped = false;

    @HostBinding('attr.aria-valuemin')
    @Input()
    public minvalue = 0;

    @HostBinding('attr.aria-valuemax')
    @Input()
    public maxvalue = 100;

    @Input()
    @HostBinding('attr.ng-aria-valuenow')
    public set value(value: number | undefined) {
        this.progress.percentage = value ?? 0;
    }

    public get value() {
        return this.progress.percentage;
    }

    @HostBinding('class')
    public get cssProgressClass() {
        const styleBuilder = ClassBuilder.create('progress-bar');
        if (this.color) {
            styleBuilder.flag('bg-', this.color);
        }
        if (this.striped) {
            styleBuilder.flag('progress-bar-', 'striped');
        }
        return styleBuilder.toString();
    }
}
