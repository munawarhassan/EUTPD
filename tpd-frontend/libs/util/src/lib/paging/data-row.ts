import * as _ from 'lodash-es';
import { PageImpl } from './page';
import { objectPath, Path } from '../utils';
import { Pageable } from './pageable';
import { Page } from './page';

export class DataRow<T> extends PageImpl<T> {
    private _obj: unknown;
    private _path: Path;
    private _detached: boolean;
    private _original: T | undefined;
    private _selected: T | undefined;
    private _isNew = false;

    public static addToArray(obj: unknown, path: Path, item: unknown): unknown[] {
        return objectPath.push(obj, path, item);
    }

    public static removeFromArray(obj: unknown, path: Path, item: unknown) {
        objectPath.remove(obj, path, item);
    }

    constructor(object: unknown, path: Path, detached = true, size = 10) {
        super([], Pageable.of(0, size));
        this._obj = object;
        this._detached = detached;
        this._path = path;
        this.clear();
    }

    public get data(): T[] {
        const values = objectPath.get(this._obj, this._path);
        if (values instanceof Array) {
            return values;
        }
        return [];
    }

    public get pageable(): Pageable {
        return super.pageable;
    }

    public set pageable(value: Pageable) {
        this.setPageable(value);
        this.setContent(Page.slice(this.data, value), value, this.data.length);
    }

    public get original(): T | undefined {
        return this._original;
    }

    public get selected(): T | undefined {
        return this._selected;
    }

    public set selected(item: T | undefined) {
        this.clear();
        this._original = item;
        if (this.detached) {
            this._selected = _.cloneDeep(item);
        } else {
            this._selected = item;
        }
        this._isNew = false;
    }

    public get number(): number {
        return super.number;
    }

    public set number(value: number) {
        this.setContent(
            Page.slice(this.data, { pageNumber: value, pageSize: this.size }),
            Pageable.ofPageable(value, this.pageable),
            this.data.length
        );
    }

    public get detached(): boolean {
        return this._detached;
    }

    public get isNew(): boolean {
        return this._isNew;
    }

    public sync(): void {
        this.number = 0;
    }

    public clear() {
        this._original = undefined;
        this._selected = undefined;
        this._isNew = false;
    }

    public isSelected(item?: T): boolean {
        if (!item) return this.selected != null;
        return this.original === item;
    }

    public add(item?: T): T {
        this.clear();

        this.selected = item || ({} as any);
        if (this.detached) {
            this._original = undefined;
        } else {
            this._original = this.selected;
            DataRow.addToArray(this._obj, this._path, this.selected);
        }
        this._isNew = true;
        this.sync();
        return this.selected as T;
    }

    public sortBy(compareFn?: (a: T, b: T) => number) {
        const arr = objectPath.get(this._obj, this._path);
        this.setContent(arr.sort(compareFn), this.pageable, this.data.length);
    }

    public remove(item?: T): void {
        if (!item) {
            item = this.original;
        }
        DataRow.removeFromArray(this._obj, this._path, item);
        this.sync();
        this.clear();
    }

    public cancel(): void {
        if (this.isNew && this.selected) {
            const index = this.data.findIndex((item) => item === this.selected);
            if (index !== -1) {
                DataRow.removeFromArray(this._obj, this._path, this.selected);
            }
            this.clear();
        }
    }

    public update(item?: T) {
        if (!this.detached) return;
        if (item) {
            this._selected = item;
        }
        if (this._isNew) {
            // add presentation
            DataRow.addToArray(this._obj, this._path, this.selected);
            // set update state
            this._original = this.selected;
            this._selected = _.cloneDeep(this.original);
        } else {
            if (this.original) {
                // update
                this._original = _.merge(this.original, this.selected);
                this._selected = _.cloneDeep(this.original);
            }
        }
        this.sync();
        this._isNew = false;
    }
}
