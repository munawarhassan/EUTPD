import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    DoCheck,
    ElementRef,
    EventEmitter,
    HostBinding,
    Input,
    KeyValueDiffer,
    KeyValueDiffers,
    OnInit,
    Output,
} from '@angular/core';
import { Page, Pageable } from '@devacfr/util';
import { DefaultPaginationConfig, PaginationOptions } from './pagination.config';
import { PageChangedEvent } from './typing';

type PageType = {
    number: number;
    text: string;
    active: boolean;
};

@Component({
    selector: 'lt-pagination',
    templateUrl: './pagination.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaginationComponent implements OnInit, DoCheck {
    @HostBinding('class')
    public class = 'd-flex justify-content-between';
    @Input()
    public page: Page<unknown> | undefined;

    /** limit number for page links in pager */
    @Input()
    public maxSize: number;
    /** if false first and last buttons will be hidden */
    @Input()
    public boundaryLinks: boolean;

    @Input()
    public directionLinks: boolean;

    /** if true current page will in the middle of pages list */
    @Input()
    public rotate: boolean;

    /** if true pagination component will be disabled */
    @Input()
    public disabled = false;

    /** fired when page was changed, $event:{page, itemsPerPage} equals to object
     * with current page index and number of items per page
     */
    @Output()
    public pageChanged = new EventEmitter<PageChangedEvent>();

    public pageSizes = [10, 20, 30];
    public pageSize: number;
    public classMap: string | undefined;
    public pages: PageType[] | undefined;

    private config: PaginationOptions;

    private pageDiffer: KeyValueDiffer<string, unknown> | undefined;

    constructor(private _differs: KeyValueDiffers, private _elementRef: ElementRef, private _cd: ChangeDetectorRef) {
        this._elementRef = _elementRef;
        this.config = Object.assign({}, DefaultPaginationConfig);
        this.pageSize = 20;
        // watch for maxSize
        this.maxSize = this.config.maxSize == null ? 20 : this.config.maxSize;
        this.rotate = this.config.rotate;
        this.boundaryLinks = this.config.boundaryLinks;
        this.directionLinks = this.config.directionLinks;
    }

    public ngDoCheck(): void {
        if (!this.pageDiffer || this.page === undefined) {
            return;
        }
        const changes = this.pageDiffer.diff(this.page);
        if (changes) {
            this.onChanged();
        }
    }

    public onChanged() {
        if (this.page === undefined) {
            return;
        }
        this.pageSize = this.page.size;
        this.pages = this.getPages(this.page.number + 1, this.page.totalPages);
        this._cd.markForCheck();
    }

    public ngOnInit(): void {
        if (typeof window !== 'undefined') {
            this.classMap = this._elementRef.nativeElement.getAttribute('class') || '';
        }

        this.pageDiffer = this._differs.find(this.page).create();
    }

    public handleSelectPage(page: Pageable | number, event: Event): void {
        if (event) {
            event.preventDefault();
        }
        const a = event.currentTarget as HTMLElement;
        const disabled = a.getAttribute('disabled');
        if (disabled === 'true') {
            return;
        }
        if (!this.disabled) {
            if (event && event.target) {
                const target = event.target as HTMLElement;
                target.blur();
            }
            let p;
            if (page instanceof Pageable) {
                p = page;
            } else if (typeof page === 'number' && this.page) {
                p = Pageable.of(
                    page - 1,
                    this.pageSize,
                    this.page.pageable?.filters,
                    this.page.pageable?.search,
                    ...this.page.sort
                );
            }
            if (p) {
                this.pageChanged.emit({
                    target: this,
                    pageable: p,
                });
            }
        }
    }

    public handleChangeSize() {
        if (this.page === undefined) {
            return;
        }
        this.pageChanged.emit({
            target: this,
            pageable: Pageable.of(
                0,
                this.pageSize,
                this.page.pageable?.filters,
                this.page.pageable?.search,
                ...this.page.sort
            ),
        });
    }

    // Create page object used in template
    protected makePage(num: number, text: string, active: boolean): PageType {
        return { text, number: num, active };
    }

    protected getPages(currentPage: number, totalPages: number): PageType[] {
        const pages: PageType[] = [];

        // Default page limits
        let startPage = 1;
        let endPage = totalPages;
        const maxSize = Number(this.maxSize);
        const isMaxSized = typeof maxSize !== 'undefined' && maxSize < totalPages;

        // recompute if maxSize
        if (isMaxSized) {
            if (this.rotate) {
                // Current page is displayed in the middle of the visible ones
                startPage = Math.max(currentPage - Math.floor(maxSize / 2), 1);
                endPage = startPage + maxSize - 1;

                // Adjust if limit is exceeded
                if (endPage > totalPages) {
                    endPage = totalPages;
                    startPage = endPage - maxSize + 1;
                }
            } else {
                // Visible pages are paginated with maxSize
                startPage = (Math.ceil(currentPage / maxSize) - 1) * maxSize + 1;

                // Adjust last page if limit is exceeded
                endPage = Math.min(startPage + maxSize - 1, totalPages);
            }
        }

        // Add page number links
        for (let num = startPage; num <= endPage; num++) {
            const page = this.makePage(num, num.toString(), num === currentPage);
            pages.push(page);
        }

        // Add links to move between page sets
        if (isMaxSized && !this.rotate) {
            if (startPage > 1) {
                const previousPageSet = this.makePage(startPage - 1, '...', false);
                pages.unshift(previousPageSet);
            }

            if (endPage < totalPages) {
                const nextPageSet = this.makePage(endPage + 1, '...', false);
                pages.push(nextPageSet);
            }
        }

        return pages;
    }
}
