/* eslint-disable max-classes-per-file */
import { HttpParams } from '@angular/common/http';
import { Order } from './page-order';
import { Optional } from '../utils';
import {
    FilterAccessor,
    FilterTerm,
    FilterTerms,
    PageDirection,
    QueryOperator,
    SortAccessor,
    SortDirection,
} from './typing';
import _ from 'lodash-es';

export class Pageable {
    /** the sorting parameters */
    private _sort: Order[];

    /** number of the page to be returned */
    private _page: number;
    /** the number of items to be returned */
    private _size: number;

    private _filter: FilterTerms;

    private _search: string | undefined;

    public static buildQuery(searchTerm: string | undefined): string | undefined {
        if (searchTerm && searchTerm.length > 0) {
            searchTerm = searchTerm.toLocaleLowerCase();
            if (searchTerm.charAt(searchTerm.length - 1) !== '*') {
                searchTerm += '*';
            }
        }
        return searchTerm;
    }

    public static buildSort(value: string): Order[] {
        if (!value) {
            return [];
        }
        const ar = value.split('|');
        return ar.map((order) => {
            const direction = order.charAt(0) === '-' ? 'DESC' : 'ASC';
            let property = order;
            if (direction === 'DESC') {
                property = property.substring(1);
            }
            return Order.of(direction, property);
        });
    }

    public static buildFilter(filter: string): FilterTerms {
        if (!filter) {
            return {};
        }
        const ar = filter.split('|');
        const matcher = /(.*)::(.*)==(.*)/;

        const filterTerms: FilterTerms = {};
        ar.forEach((f) => {
            const matches = matcher.exec(f);
            if (matches && matches.length > 0) {
                const op = matches[1] as keyof typeof QueryOperator;
                const val = matches[2];
                const term: FilterTerm = {
                    values: val.split(','),
                    op: QueryOperator[op],
                };
                if (filterTerms[matches[0]]) {
                    filterTerms[matches[0]].push(term);
                } else {
                    filterTerms[matches[0]] = [term];
                }
            }
        });
        return filterTerms;
    }

    public static unpaged(): Pageable {
        return unpaged;
    }

    public static ofPageable(page: number, { pageSize, filters, search, sort }: Pageable): Pageable {
        return new Pageable(page, pageSize, filters, search, ...sort);
    }

    public static of(page = 0, size = 20, filters: FilterTerms = {}, search?: string, ...sort: Order[]): Pageable {
        return new Pageable(page, size, filters, search, ...sort);
    }

    public static ofProperty(
        page: number,
        size: number,
        filters: FilterTerms = {},
        direction: PageDirection = 'ASC',
        ...properties: string[]
    ): Pageable {
        return Pageable.of(page, size, filters, undefined, ...Order.by(direction, ...properties));
    }

    constructor(page: number, size: number, filter: FilterTerms = {}, search?: string, ...sort: Order[]) {
        this._page = page;
        this._size = size;
        this._sort = _.cloneDeep(sort) || [];
        this._filter = _.cloneDeep(filter);
        this._search = search;
    }

    public copy(): Pageable {
        return new Pageable(this._page, this._size, this._filter, this._search, ...this._sort);
    }

    public isPaged(): boolean {
        return true;
    }

    public isUnpaged(): boolean {
        return !this.isPaged();
    }

    /** the number of the page to be returned */
    public get pageNumber(): number {
        return this._page;
    }

    /** Gets the number of items to be returned */
    public get pageSize(): number {
        return this._size;
    }

    /** Sets the number of items to be returned */
    public set pageSize(v: number) {
        this._size = v;
    }

    public get search(): string | undefined {
        return this._search;
    }

    public set search(v: string | undefined) {
        this._search = v;
    }

    public get sort(): Order[] {
        return this._sort;
    }

    /** the sorting parameters */
    public order(): SortAccessor<Pageable> {
        return {
            clear: (): SortAccessor<Pageable> => {
                this._sort = [];
                return this.order();
            },
            get: (property: string): Order | undefined => {
                return this._sort.find((order) => order.property === property);
            },
            set: (property: string, direction: PageDirection = 'ASC', ignoreCase = false): SortAccessor<Pageable> => {
                const order = Order.of(direction, property);
                if (ignoreCase) {
                    order.setIngoreCase();
                }
                this._sort.push(order);
                return this.order();
            },
            has: (property: string): boolean => {
                return this._sort.find((order) => order.property === property) !== undefined;
            },
            remove: (property?: string): SortAccessor<Pageable> => {
                if (!property) {
                    this._sort = [];
                } else {
                    const order = this.order().get(property);
                    if (order) {
                        this._sort.slice(this._sort.indexOf(order));
                    }
                }
                return this.order();
            },
            isDescending(property: string): boolean {
                if (property) {
                    const order = this.get(property);
                    return order ? order.direction === 'ASC' : false;
                }
                return false;
            },
            isAscending(property: string): boolean {
                if (property) {
                    const order = this.get(property);
                    return order ? order.direction === 'DESC' : false;
                }
                return false;
            },
            end: (): Pageable => {
                return this;
            },
        };
    }

    /** the offset to be taken according to the underlying page and page size. */
    public get offset(): number {
        return this.pageNumber * this.pageSize;
    }

    public get filters(): FilterTerms {
        return this._filter;
    }

    public clearFilter(): Pageable {
        this._filter = {};
        return this;
    }

    public removeFilter(field: string): Pageable {
        delete this._filter[field];
        return this;
    }

    public filter(): FilterAccessor<Pageable> {
        return {
            op: (
                property: string,
                op: QueryOperator,
                not = false,
                ...terms: string[] | undefined[]
            ): FilterAccessor<Pageable> => {
                let operator = op;
                if (not) {
                    switch (op) {
                        case QueryOperator.equals:
                            operator = QueryOperator.notEquals;
                            break;
                        default:
                            break;
                    }
                }
                this._filterFn(property, operator, ...terms);
                return this.filter();
            },
            contains: (property: string, term: string): FilterAccessor<Pageable> => {
                this._filterFn(property, QueryOperator.contains, term);
                return this.filter();
            },
            eq: (property: string, term: string): FilterAccessor<Pageable> => {
                this._filterFn(property, QueryOperator.equals, term);
                return this.filter();
            },
            notEq: (property: string, term: string): FilterAccessor<Pageable> => {
                this._filterFn(property, QueryOperator.notEquals, term);
                return this.filter();
            },
            start: (property: string, term: string): FilterAccessor<Pageable> => {
                this._filterFn(property, QueryOperator.start, term);
                return this.filter();
            },
            end: (property: string, term: string): FilterAccessor<Pageable> => {
                this._filterFn(property, QueryOperator.end, term);
                return this.filter();
            },
            lte: (property: string, term: string): FilterAccessor<Pageable> => {
                this._filterFn(property, QueryOperator.lte, term);
                return this.filter();
            },
            gte: (property: string, term: string): FilterAccessor<Pageable> => {
                this._filterFn(property, QueryOperator.gte, term);
                return this.filter();
            },
            exists: (property: string, term: string): FilterAccessor<Pageable> => {
                this._filterFn(property, QueryOperator.exists, term);
                return this.filter();
            },
            before: (property: string, term: string): FilterAccessor<Pageable> => {
                this._filterFn(property, QueryOperator.before, term);
                return this.filter();
            },
            after: (property: string, term: string): FilterAccessor<Pageable> => {
                this._filterFn(property, QueryOperator.after, term);
                return this.filter();
            },
            between: (property: string, from: string, to: string): FilterAccessor<Pageable> => {
                this._filterFn(property, QueryOperator.after, from, to);
                return this.filter();
            },
            in: (property: string, ...terms: string[]): FilterAccessor<Pageable> => {
                this._filterFn(property, QueryOperator.in, ...terms);
                return this.filter();
            },
            notIn: (property: string, ...terms: string[]): FilterAccessor<Pageable> => {
                this._filterFn(property, QueryOperator.notIn, ...terms);
                return this.filter();
            },
            back: (): Pageable => {
                return this;
            },
        };
    }

    private _filterFn(property: string, op?: QueryOperator, ...values: (string | undefined)[]): Pageable {
        if (!this._filter) {
            this._filter = {};
        }
        if (op == null) {
            op = QueryOperator.equals;
        }
        const term: FilterTerm = {
            values: values,
            op,
        };
        if (this._filter[property]) {
            this._filter[property].push(term);
        } else {
            this._filter[property] = [term];
        }
        return this;
    }

    /**
     * Returns whether there's a previous Pageable we can access from the current one.
     * Will return false in case the current Pageable already refers to the first page.
     */
    public get hasPrevious(): boolean {
        return this.pageNumber > 0;
    }

    /** the previous Pageable or the first Pageable if the current one already is the first one. */
    public previousOrFirst(): Pageable {
        return this.hasPrevious ? this.previous() : this.first();
    }

    /** the Pageable requesting the next Page. */
    public next(): Pageable {
        return new Pageable(this.pageNumber + 1, this.pageSize, this._filter, this.search, ...this._sort);
    }

    /** the previous Pageable or the first Pageable if the current one already is the first one. */
    public previous(): Pageable {
        return this.pageNumber === 0
            ? this
            : new Pageable(this.pageNumber - 1, this.pageSize, this._filter, this.search, ...this._sort);
    }

    /** the Pageable requesting the first page. */
    public first(): Pageable {
        return new Pageable(0, this.pageSize, this._filter, this.search, ...this._sort);
    }

    public current(): Pageable {
        return new Pageable(this.pageNumber, this.pageSize, this._filter, this.search, ...this._sort);
    }

    public toOptional(): Optional<Pageable> {
        return this.isUnpaged() ? Optional.empty() : Optional.of(this);
    }

    public httpParams(params?: HttpParams): HttpParams {
        if (params == null) {
            params = new HttpParams();
        }
        const sort = this.buildSort(this._sort);
        const filter = this.buildFilter(this._filter);

        params = params.append('page', this.pageNumber.toString()).append('size', this.pageSize.toString());
        if (sort) {
            params = params.append('sort', sort);
        }
        if (filter) {
            params = params.append('filter', filter);
        }
        if (this.search) {
            params = params.append('search', this.search);
        }
        return params;
    }

    private buildSort(sort: Order[]) {
        let val = '';
        if (sort) {
            let first = true;
            sort.forEach((order) => {
                if (!first) {
                    val += '|';
                }
                if (order.ignoreCase) {
                    val += '!';
                }
                if (order.direction === 'DESC') {
                    val += SortDirection.Descending;
                } else if (order.direction === 'ASC') {
                    val += SortDirection.Ascending;
                } else {
                    val += SortDirection.None;
                }
                val += order.property;
                first = false;
            });
        }
        return val === '' ? null : encodeURIComponent(val);
    }

    private buildFilter(filter: FilterTerms): string | null {
        if (!filter) {
            return null;
        }
        const val: string[] = [];
        if (filter) {
            for (const property in filter) {
                if (Object.prototype.hasOwnProperty.call(filter, property)) {
                    const terms = filter[property];
                    if (!terms) {
                        continue;
                    }
                    terms.forEach((term) => {
                        if (term.values && term.values.length) {
                            val.push(`${property}::${term.op}==${term.values.join(',')}`);
                        }
                    });
                }
            }
        }
        return val.length === 0 ? null : encodeURIComponent(val.join('|'));
    }
}

class Unpage extends Pageable {
    constructor() {
        super(0, 0);
    }

    public isPaged() {
        return false;
    }

    public get pageNumber(): number {
        return 0;
    }

    public get pageSize(): number {
        return 0;
    }

    public set pageSize(v: number) {
        throw new Error('Unsupported Operation');
    }

    public get sort(): Order[] {
        return Order.unsorted();
    }

    public order(): SortAccessor<Pageable> {
        return {
            clear: (): SortAccessor<Pageable> => {
                return this.order();
            },
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            get(this: Pageable, property: string): Order | undefined {
                return undefined;
            },
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            set(property: string, direction: PageDirection): SortAccessor<Pageable> {
                return this;
            },
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            has(property: string): boolean {
                return false;
            },
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            remove(property?: string): SortAccessor<Pageable> {
                return this;
            },
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            isDescending(property: string): boolean {
                return false;
            },
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            isAscending(property: string): boolean {
                return true;
            },
            end: (): Pageable => {
                return this;
            },
        };
    }

    public filter(): FilterAccessor<Pageable> {
        return {
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            op: (
                property: string,
                op: QueryOperator,
                not = false,
                ...terms: (string | undefined)[]
            ): FilterAccessor<Pageable> => {
                return this.filter();
            },
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            contains: (property: string, term: string): FilterAccessor<Pageable> => {
                return this.filter();
            },
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            eq: (property: string, term: string): FilterAccessor<Pageable> => {
                return this.filter();
            },
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            notEq: (property: string, term: string): FilterAccessor<Pageable> => {
                return this.filter();
            },
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            start: (property: string, term: string): FilterAccessor<Pageable> => {
                return this.filter();
            },
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            end: (property: string, term: string): FilterAccessor<Pageable> => {
                return this.filter();
            },
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            lte: (property: string, term: string): FilterAccessor<Pageable> => {
                return this.filter();
            },
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            gte: (property: string, term: string): FilterAccessor<Pageable> => {
                return this.filter();
            },
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            exists: (property: string, term: string): FilterAccessor<Pageable> => {
                return this.filter();
            },
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            before: (property: string, term: string): FilterAccessor<Pageable> => {
                return this.filter();
            },
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            after: (property: string, term: string): FilterAccessor<Pageable> => {
                return this.filter();
            },
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            between: (property: string, from: string, to: string): FilterAccessor<Pageable> => {
                return this.filter();
            },
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            in: (property: string, ...terms: string[]): FilterAccessor<Pageable> => {
                return this.filter();
            },
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            notIn: (property: string, ...terms: string[]): FilterAccessor<Pageable> => {
                return this.filter();
            },
            back: (): Pageable => {
                return this;
            },
        };
    }

    public get offset(): number {
        throw new Error('Unsupported Operation');
    }
    public get hasPrevious(): boolean {
        return false;
    }
    public previousOrFirst(): Pageable {
        return this;
    }
    public next(): Pageable {
        return this;
    }
    public previous(): Pageable {
        throw new Error('Unsupported Operation');
    }

    public first(): Pageable {
        return this;
    }
}

const unpaged = new Unpage();
