import { Order, Page, Pageable } from '.';

export enum SortDirection {
    Ascending = '+',
    Descending = '-',
    None = '',
}

export enum QueryOperator {
    equals = 'eq',
    notEquals = 'noteq',
    contains = 'contains',
    start = 'start',
    end = 'end',
    lte = 'lte',
    gte = 'gte',
    exists = 'exists',
    before = 'before',
    after = 'after',
    between = 'between',
    in = 'in',
    notIn = 'notin',
}

export interface FilterTerm {
    values: (string | undefined)[];
    op: QueryOperator;
}

export interface FilterTerms {
    [key: string]: FilterTerm[];
}

export interface SortProperties {
    [key: string]: SortDirection;
}

export type PageDirection = 'ASC' | 'DESC';

export interface SortAccessor<T> {
    clear(): SortAccessor<T>;
    get(property: string): Order | undefined;
    set(property: string, direction?: PageDirection, ignoreCase?: boolean): SortAccessor<T>;
    has(property: string): boolean;
    remove(property?: string): SortAccessor<T>;
    isAscending(property: string): boolean;
    isDescending(property: string): boolean;
    end(): T;
}

export interface FilterAccessor<T> {
    op(
        property: string,
        op: QueryOperator,
        not: boolean | undefined,
        ...term: (string | undefined)[]
    ): FilterAccessor<T>;
    contains(property: string, term: string): FilterAccessor<T>;
    eq(property: string, term: string): FilterAccessor<T>;
    notEq(property: string, term: string): FilterAccessor<T>;
    start(property: string, term: string): FilterAccessor<T>;
    end(property: string, term: string): FilterAccessor<T>;
    lte(property: string, term: string): FilterAccessor<T>;
    gte(property: string, term: string): FilterAccessor<T>;
    exists(property: string, term: string): FilterAccessor<T>;
    before(property: string, term: string): FilterAccessor<T>;
    after(property: string, term: string): FilterAccessor<T>;
    between(property: string, from: string, to: string): FilterAccessor<T>;
    in(property: string, ...terms: string[]): FilterAccessor<T>;
    notIn(property: string, ...terms: string[]): FilterAccessor<T>;
    back(): T;
}

export interface PageInterface<T> {
    readonly content: T[];
    /**   the total amount of elements. */
    readonly totalElements: number;
    /** the number of total pages. */
    readonly totalPages: number;
    /**  the size of the Page. */
    readonly size: number;
    /** the number of the current Page. Is always non-negative. */
    // eslint-disable-next-line id-blacklist
    readonly number: number;
    /** the number of elements currently on this Page. */
    readonly numberOfElements: number;
    /** whether the current Page is the first one */
    readonly first: boolean;
    /** whether the current Page is the last one. */
    readonly last: boolean;
    /**  the sorting parameters for the Page. */
    readonly sort: Order[];

    readonly pageable: Pageable;

    map<U>(converter: (value: T) => U): Page<U>;
}
