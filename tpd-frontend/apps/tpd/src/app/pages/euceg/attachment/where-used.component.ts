import { KeyValue } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, HostBinding, Input } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { AttachmentList, ProductList, ProductService } from '@devacfr/euceg';
import { NotifierService } from '@devacfr/layout';
import { Page, Pageable } from '@devacfr/util';
import { BehaviorSubject, EMPTY, Observable } from 'rxjs';
import { distinctUntilChanged, finalize, map, switchMap, tap } from 'rxjs/operators';

@Component({
    selector: 'app-where-used',
    templateUrl: './where-used.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WhereUsedComponent {
    @HostBinding('class')
    public class = 'card card-xl-stretch mb-5 mb-xl-8 w-100';

    public page$: Observable<Map<number, Page<ProductList>>>;

    public fetchedPages: Map<number, Page<ProductList>> = new Map();

    public loading = false;

    private _currentPageable$: BehaviorSubject<Pageable>;
    private _selectedWhereUsed: AttachmentList | undefined;
    public _currentPage = 0;

    constructor(
        public svgIcons: SvgIcons,
        private _productService: ProductService,
        private _notifierService: NotifierService,
        private _cd: ChangeDetectorRef
    ) {
        const request = Pageable.of(0, 20).order().set('productNumber', 'ASC', true).end();
        this._currentPageable$ = new BehaviorSubject<Pageable>(request);
        this.page$ = this._currentPageable$.pipe(
            distinctUntilChanged(),
            tap(() => {
                this.loading = true;
            }),
            switchMap((pageable) =>
                this._selectedWhereUsed?.attachmentId
                    ? this._productService.whereUsed(this._selectedWhereUsed.attachmentId, pageable).pipe(
                          map((page) => {
                              this._currentPage = page.number;
                              this.fetchedPages.set(page.number, page);
                              return this.fetchedPages;
                          }),
                          finalize(() => {
                              this.loading = false;
                          }),
                          this._notifierService.catchError()
                      )
                    : EMPTY
            )
        );
    }

    public get selected(): AttachmentList | undefined {
        return this._selectedWhereUsed;
    }

    @Input()
    public set selected(att: AttachmentList | undefined) {
        this._selectedWhereUsed = att;
        this._currentPage = 0;
        this.fetchedPages.clear();
        this._currentPageable$.next(this.currentPageable.first());
    }

    public get currentPage(): Page<ProductList> | undefined {
        return this.fetchedPages.get(this._currentPage);
    }

    public get currentPageable(): Pageable {
        return this._currentPageable$.getValue();
    }

    public set currentPageable(pageable: Pageable) {
        this._currentPageable$.next(pageable);
    }

    public showMore(): void {
        this._currentPageable$.next(this.currentPageable.next());
    }

    public trackPage(index: number, entry: KeyValue<number, Page<ProductList>>): number {
        return entry.key;
    }

    public trackProductList(index: number, item: ProductList) {
        return item.productNumber;
    }
}
