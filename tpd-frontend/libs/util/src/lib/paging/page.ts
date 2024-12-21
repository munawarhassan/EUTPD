import { Observable, OperatorFunction } from 'rxjs';
import { map } from 'rxjs/operators';
import { Order } from './page-order';
import { Pageable } from './pageable';
import { PageInterface, SortAccessor } from './typing';

export interface PageMethod {
    order(): SortAccessor<Pageable>;

    hasPrevious(): boolean;

    hasNext(): boolean;

    firstPageable(): Pageable;

    lastPageable(): Pageable;

    nextPageable(): Pageable;

    previousPageable(): Pageable;
}

export interface Page<T> extends PageInterface<T>, PageMethod {}

// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace Page {
    export function slice<T>(data: T[], pageable: { pageNumber: number; pageSize: number }): T[] {
        const begin = pageable.pageNumber * pageable.pageSize;
        const end = begin + pageable.pageSize;
        const values = data.slice(begin, end);
        return values;
    }

    export function empty<E>(pageable?: Pageable): Page<E> {
        if (!pageable) {
            pageable = Pageable.unpaged();
        }
        return new PageImpl<E>([], pageable, 0);
    }

    export function of<E>(page: { content: E[]; pageable?: Pageable; totalElements?: number }): Page<E> {
        let data = page.content;
        if (page.pageable && data.length > page.pageable.pageSize) {
            data = Page.slice(data, page.pageable);
        }
        return new PageImpl<E>(data, page.pageable, page.totalElements ? page.totalElements : page.content.length);
    }

    export function mapOf<T>(pageable: Pageable): OperatorFunction<Page<T>, Page<T>> {
        return map((page) =>
            Page.of({
                content: page.content,
                pageable: pageable.copy(),
                totalElements: page.totalElements ? page.totalElements : page.content.length,
            })
        );
    }
}

export type PageObserver<T> = (obs: Observable<Pageable>) => Observable<Page<T>>;

export class PageImpl<T> implements Page<T> {
    /** content of page */
    private _content!: T[];

    private _pageable!: Pageable;

    private _total!: number;

    constructor(content: T[], pageable?: Pageable, total?: number) {
        this.setContent(content, pageable, total);
    }

    protected setContent(content: T[], pageable?: Pageable, total?: number): void {
        this._content = content || [];
        this._pageable = pageable ? pageable : Pageable.unpaged();
        if (total == null) {
            total = !content ? 0 : content.length;
        }
        this._total = this._pageable
            .toOptional()
            .filter(() => content.length > 0)
            .filter((it) => it.offset + it.pageSize > (total as number))
            .map((it) => it.offset + content.length)
            .orElse(total);
    }

    /**   the total amount of elements. */
    public get totalElements(): number {
        return this._total;
    }
    /** the number of total pages. */
    public get totalPages(): number {
        return this.size === 0 ? 1 : Math.ceil(this._total / this.size);
    }

    public hasPrevious(): boolean {
        return this.number > 0;
    }

    public hasNext(): boolean {
        return this.number + 1 < this.totalPages;
    }

    public get first(): boolean {
        return !this.hasPrevious();
    }

    public get last(): boolean {
        return !this.hasNext();
    }

    /**  the size of the Page. */
    public get size(): number {
        return this._pageable.isPaged() ? this._pageable.pageSize : 0;
    }

    /** the number of the current Page. Is always non-negative. */
    public get number(): number {
        return this._pageable.isPaged() ? this._pageable.pageNumber : 0;
    }

    /** the number of elements currently on this Page. */
    public get numberOfElements(): number {
        return this._content.length;
    }

    public firstPageable(): Pageable {
        return this._pageable.first();
    }
    public lastPageable(): Pageable {
        return Pageable.ofPageable(this.totalPages === 0 ? 0 : this.totalPages - 1, this._pageable);
    }

    public nextPageable(): Pageable {
        return this.hasNext() ? this._pageable.next() : Pageable.unpaged();
    }

    public previousPageable(): Pageable {
        return this.hasPrevious() ? this._pageable.previousOrFirst() : Pageable.unpaged();
    }

    public hasContent(): boolean {
        return this._content.length > 0;
    }

    public get content(): T[] {
        return this._content;
    }

    public get sort(): Order[] {
        return this._pageable.sort;
    }

    public order(): SortAccessor<Pageable> {
        return this._pageable.order();
    }

    public map<U>(converter: (value: T) => U): Page<U> {
        return new PageImpl<U>(this._content.map(converter), this._pageable, this._total);
    }

    public get pageable(): Pageable {
        return this._pageable;
    }

    protected setPageable(v: Pageable) {
        this._pageable = v;
    }
}
