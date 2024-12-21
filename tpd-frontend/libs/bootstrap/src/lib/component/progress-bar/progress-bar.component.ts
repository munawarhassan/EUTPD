import { Component, ContentChildren, HostBinding, Input, QueryList } from '@angular/core';
import { BsColor, ClassBuilder, Progress } from '@devacfr/util';
import { ProgressBarItemComponent } from './progress-bar-item.component';

@Component({
    selector: 'lt-progress-bar',
    templateUrl: './progress-bar.component.html',
})
export class ProgressBarComponent {
    @HostBinding('class')
    @Input()
    public set class(value: string) {
        this.class = value;
    }

    public get class(): string {
        const builder = ClassBuilder.create('align-items-center d-flex flex-column');
        if (this._class) builder.css(this._class);
        return builder.toString();
    }
    /**
     * the size of progresbar (sm,lg), default is 'md'.
     */
    @Input()
    public size: string | number = 10;

    @Input()
    public color: BsColor = 'primary';

    /**
     * display the progress in striped mode
     */
    @Input()
    public striped = false;

    /**
     * the progress model used
     */
    @Input()
    public progress: Progress = new Progress();

    /**
     *
     */
    @Input()
    public set value(value: number | undefined) {
        this.progress.percentage = value ?? 0;
    }

    @Input()
    public enableLabel = false;

    @ContentChildren(ProgressBarItemComponent)
    public items!: QueryList<ProgressBarItemComponent>;

    private _class = '';

    public getSize(): string {
        if (typeof this.size == 'string') return this.size;
        return String(this.size) + 'px';
    }

    public getColorCss(): string {
        const builder = ClassBuilder.create('progress-bar');
        if (this.color) builder.flag('bg-', this.color);
        if (this.striped) builder.flag('progress-bar-', 'striped animated');
        return builder.toString();
    }
}
