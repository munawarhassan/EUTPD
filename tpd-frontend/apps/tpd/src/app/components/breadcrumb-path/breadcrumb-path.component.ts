import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { BsColor, ClassBuilder } from '@devacfr/util';
import { isArray } from 'lodash-es';

@Component({
    selector: 'app-breadcrumb-path',
    templateUrl: 'breadcrumb-path.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BreadcrumbPathComponent {
    @Input()
    public disabled = false;

    @Input()
    public set path(value: string | string[]) {
        this._path = this.splitPath(value);
    }

    public get path(): string | string[] {
        return this._path;
    }

    @Input()
    public lastLink = false;

    @Input()
    public size = 'lg';

    @Input()
    public color: BsColor = 'primary';

    @Output()
    public pathChange = new EventEmitter<string | string[]>();

    private _path: string[] = [];

    public get badgeClass(): string {
        const builder = ClassBuilder.create('badge');
        if (this.size) builder.flag('badge-', this.size);
        if (this.color) builder.flag(' badge-light-', this.color);
        return builder.toString();
    }

    constructor(public svgIcons: SvgIcons) {}

    public getPath(): string[] {
        return this._path;
    }

    public setPath(path: string[]): void {
        this.pathChange.emit(path);
    }

    public splitPath(path: string | string[]): string[] {
        if (isArray(path)) {
            return path;
        }
        if (path.length === 0) {
            return [];
        }
        const ar = path.split('/');
        return ar;
    }
}
