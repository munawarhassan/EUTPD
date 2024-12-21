import { KeyValue } from '@angular/common';
import {
    AfterViewInit,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    ElementRef,
    EventEmitter,
    HostBinding,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    Output,
    Renderer2,
    SimpleChanges,
    TemplateRef,
    ViewChild,
} from '@angular/core';
import { AutoResizeOptions } from '@devacfr/bootstrap';
import { ClassBuilder, Page, PageDirection, PageObserver, Pageable } from '@devacfr/util';
import { uniqueId } from 'lodash-es';
import { InfiniteScrollDirective } from 'ngx-infinite-scroll';
import { NgScrollbar } from 'ngx-scrollbar';
import { BehaviorSubject, EMPTY, Observable, Subscription, of } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { PageChangedEvent } from '../pagination';
import { TableColumn, TableOptions } from './typing';

function isSortable(column: TableColumn): boolean {
    if (typeof column.sort === 'boolean') {
        return column.sort;
    }
    return column.sort != null;
}

@Component({
    selector: 'lt-table',
    templateUrl: './table.component.html',
    styleUrls: ['./table.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TableComponent implements OnInit, AfterViewInit, OnChanges, OnDestroy {
    @HostBinding('id')
    @Input()
    public id = uniqueId('lt_table');

    @Input()
    public layer: 'card' | 'line' = 'line';

    @Input()
    public tableClass = 'bg-body gx-xl-6 gy-xl-4 gx-3 gy-4 fw-bold rounded-1';

    @Input()
    public page: Page<unknown> | PageObserver<unknown> | undefined;

    @Input()
    public set pageable(value: Pageable | undefined) {
        if (this.pageable !== value) {
            // remove all pages when pageable request change outside of component
            this.fetchedPages.clear();
            this._pageable$.next(value);
        }
    }

    public get pageable(): Pageable | undefined {
        return this._pageable$.getValue();
    }

    @Input()
    public options: TableOptions = {
        minThumbSize: 20,
        columns: [],
    };

    @Input()
    public lineTemplate: TemplateRef<{ page: Page<unknown> }> | undefined;

    @Input()
    public cardTemplate: TemplateRef<{ page: Page<unknown> }> | undefined;

    @Output()
    public pageChanged = new EventEmitter<PageChangedEvent>();

    @Output()
    public pageSort = new EventEmitter<string>();

    @Input()
    public allChecked = false;

    @Output()
    public allCheckedChange = new EventEmitter<boolean>();

    @Input()
    public autoResizeOptions?: AutoResizeOptions = { activate: false };

    public get responsiveClass(): string {
        const builder = ClassBuilder.create('table-responsive');
        // if (this.layer === 'line') {
        //     builder.css('table-header--fixed');
        // }
        return builder.toString();
    }

    public get currentPage(): Page<unknown> | undefined {
        return this.fetchedPages.get(this._currentPage);
    }

    public fetchedPages: Map<number, Page<unknown>> = new Map();

    public page$: Observable<Page<unknown>> | undefined;
    public totalElements = 0;

    @ViewChild(NgScrollbar)
    private scrollComponent!: NgScrollbar;

    private _pageable$ = new BehaviorSubject<Pageable | undefined>(undefined);
    private _currentPage = 0;

    private _async = false;
    private _subscription = new Subscription();

    @ViewChild(InfiniteScrollDirective, { static: false })
    private infiniteScrollDirective?: InfiniteScrollDirective;

    constructor(private _element: ElementRef, private _cd: ChangeDetectorRef, private _renderer: Renderer2) {}

    ngOnInit(): void {
        this.page$ = this._pageable$.pipe(
            switchMap((pageable) => {
                if (!pageable || !this.page) {
                    return EMPTY;
                } else if (typeof this.page !== 'function') {
                    return of(this.page);
                } else {
                    return this.page(of(pageable));
                }
            })
        );
    }

    ngAfterViewInit(): void {
        if (this.page$) {
            this._subscription.add(
                this.page$.subscribe((page) => {
                    this.totalElements = page.totalElements;
                    // remove all pages when pagination is fixed
                    if (this.options.pagination === 'fixed' || !this.options.pagination) {
                        this.fetchedPages.clear();
                    }
                    this._currentPage = page.number;
                    this.fetchedPages.set(page.number, page);
                    // this.allCheckboxSelected = false;
                    this.updateSorting();
                    this._cd.detectChanges();
                })
            );
        }
    }

    public onInfiniteScroll() {
        if (this.options.pagination === 'infinite') {
            if (!this.currentPage || this.currentPage?.last) {
                return;
            }
            const request = this.currentPage?.nextPageable();
            this.handleChanged({ target: this, pageable: request });
        }
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.page && changes.page.currentValue !== changes.page.previousValue) {
            this._async = typeof this.page === 'function';
            this.finalizeSyncCall();
        }
        this._cd.detectChanges();
    }

    ngOnDestroy(): void {
        this.fetchedPages.clear();
        this._subscription.unsubscribe();
    }

    protected finalizeSyncCall(): void {
        // if pageable input property is not set
        if (!this._async && !this.pageable && this.page) {
            const page = this.page as Page<unknown>;
            this._pageable$.next(page.pageable);
        }
    }

    public trackPage(index: number, entry: KeyValue<number, Page<unknown>>): number {
        return entry.key;
    }

    public refresh(): void {
        if (this.pageable) {
            this.handleChanged({ target: this, pageable: this.pageable });
        }
    }

    public update(): void {
        this.scrollComponent.update();
        this.infiniteScrollDirective?.setup();
    }

    public handleChanged(ev: PageChangedEvent): void {
        if (this._async) {
            this._pageable$.next(ev.pageable);
        }
        this.pageChanged.emit(ev);
    }

    public handleSortClick(event: Event): boolean {
        const el = event.currentTarget as HTMLElement;
        if (!el.classList.contains('column-sortable')) {
            return true;
        }
        const prop = el.getAttribute('data-column');
        if (prop) {
            el.blur();
            this.handleSort(prop);
            let request = this.pageable as Pageable;
            if (this.options.pagination === 'infinite') {
                request = request.first();
                if (this.scrollComponent) {
                    this.scrollComponent.scrollTo({ top: 0 });
                }
            }
            this.handleChanged({ target: this, pageable: request });
        }
        return false;
    }

    public addSortClass(el: HTMLElement, cls: string | undefined) {
        'column-sortable-asc column-sortable-desc'.split(' ').forEach((cl) => this._renderer.removeClass(el, cl));
        if (cls) {
            this._renderer.addClass(el, cls);
        }
        return el;
    }

    public updateSorting() {
        const el = this._element.nativeElement as HTMLElement;
        const th = el.querySelectorAll('th.column-sortable');
        th.forEach((el) => {
            const element = el as HTMLElement;
            const prop = element.getAttribute('data-column');
            if (prop) {
                const cl = this.classSort(prop);
                this.addSortClass(element, cl);
            }
        });
    }

    public getColumnClass(column: TableColumn): string {
        const css = ClassBuilder.create();
        if (isSortable(column)) {
            css.css('column-sortable');
        }
        if (column.align) {
            css.css(`text-${column.align}`);
        }
        if (column.class) {
            css.css(column.class);
        }
        return css.toString();
    }

    public getAllContents<T>(): T[] {
        return Array.from(this.fetchedPages.values()).flatMap((page) => page.content as T[]);
    }

    public getColumnTitle(column: TableColumn): string {
        if (column.i18n) return column.i18n;
        if (column.title) return column.title;
        return '';
    }

    private classSort(property: string): string | undefined {
        if (!this.pageable) {
            return;
        }
        if (this.pageable.order().isAscending(property)) {
            return 'column-sortable-asc';
        }
        if (this.pageable.order().isDescending(property)) {
            return 'column-sortable-desc';
        }
        return undefined;
    }

    private handleSort(property: string): void {
        if (!this.pageable) {
            return;
        }
        const accessor = this.pageable.order();
        let direction: PageDirection | undefined = 'ASC';
        if (accessor) {
            if (accessor.has(property)) {
                const order = accessor.get(property);
                if (order) {
                    direction = order.direction;
                    if (direction === 'ASC') {
                        direction = 'DESC';
                    } else if (direction === 'DESC') {
                        direction = undefined;
                    }
                }
            }
            accessor.remove();
            if (direction) {
                const column = this.options.columns.find((c) => c.name === property);
                const sort = typeof column?.sort !== 'boolean' ? column?.sort : undefined;
                accessor.remove().set(property, direction, sort?.ignoreCase);
            }
            this.pageSort.emit(property);
        }
    }
}
