import { KeyValue } from '@angular/common';
import {
    AfterViewInit,
    ChangeDetectorRef,
    Component,
    ElementRef,
    EventEmitter,
    HostBinding,
    Input,
    OnDestroy,
    OnInit,
    Output,
    TemplateRef,
} from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { DomSanitizer } from '@angular/platform-browser';
import { BlockUI, SvgIcons } from '@devacfr/bootstrap';
import { AuditEntity, AuditsService, Channel, ChannelType } from '@devacfr/core';
import { DaterangepickerType } from '@devacfr/forms';
import { NotifierService } from '@devacfr/layout';
import { ClassBuilder, Order, Page, Pageable } from '@devacfr/util';
import { BehaviorSubject, EMPTY, Observable, Subscription } from 'rxjs';
import { catchError, distinctUntilChanged, finalize, switchMap, tap } from 'rxjs/operators';

@Component({
    selector: 'app-audit-timeline',
    templateUrl: './audit-timeline.component.html',
})
export class AuditTimelineComponent implements OnInit, AfterViewInit, OnDestroy {
    @HostBinding('class')
    @Input()
    public get class(): string {
        const styleBuilder = ClassBuilder.create('card shadow-none');
        if (this._class) styleBuilder.css(this._class);
        return styleBuilder.toString();
    }

    public set class(value: string) {
        this._class = value;
    }

    @Input()
    public title: string | undefined;

    @Input()
    public channels: ChannelType[] = [];

    @Input()
    public range: DaterangepickerType = {};

    @Output()
    public closeAudit = new EventEmitter<void>();

    public page$: Observable<Page<AuditEntity>>;
    public fetchedPages: Map<number, Page<AuditEntity>> = new Map();

    public loading = false;

    public formControl: FormGroup;

    private _selectedChannels: Channel[] = [];

    private _currentPageable$: BehaviorSubject<Pageable>;
    private _currentPage = 0;

    private _class: string | undefined;

    private _subscription = new Subscription();

    private _block: BlockUI;

    @Input()
    public auditItemTemplate: TemplateRef<AuditEntity> | undefined;

    constructor(
        public sanitized: DomSanitizer,
        public svgIcons: SvgIcons,
        private _fb: FormBuilder,
        private _auditsService: AuditsService,
        private _notifierService: NotifierService,
        private _cd: ChangeDetectorRef,
        private _element: ElementRef<HTMLElement>
    ) {
        this._block = new BlockUI(this._element.nativeElement);
        this.formControl = this._fb.group({
            channel: ['all'],
        });
        this.channel.valueChanges.subscribe((value) => {
            switch (value) {
                case 'all':
                    this._selectedChannels = this.channels.map((ch) => ch.channel);
                    break;

                default:
                    this._selectedChannels = [value];
                    break;
            }
            this.fetchedPages.clear();
            this._currentPageable$.next(this.currentPageable.first());
        });
        const request = Pageable.of(0, 20, undefined, undefined, Order.of('DESC', 'timestamp'));
        this._currentPageable$ = new BehaviorSubject<Pageable>(request);

        this.page$ = this._currentPageable$.pipe(
            distinctUntilChanged(),
            tap(() => {
                this._block.block();
                this.loading = true;
            }),
            switchMap((currentPageble) =>
                this._auditsService
                    .findByDates(currentPageble, this.range.startDate, this.range.endDate, ...this._selectedChannels)
                    .pipe(
                        finalize(() => {
                            this._block.release();
                            this.loading = false;
                        }),
                        catchError((err) => {
                            this._notifierService.error(err);
                            return EMPTY;
                        })
                    )
            )
        );
    }

    ngOnInit(): void {
        this._selectedChannels = this.channels.map((ch) => ch.channel);
    }

    ngAfterViewInit(): void {
        this._subscription.add(
            this.page$.subscribe((page) => {
                this._currentPage = page.number;
                this.fetchedPages.set(page.number, page);
                this._cd.detectChanges();
            })
        );
    }

    ngOnDestroy(): void {
        this.fetchedPages.clear();
        this._subscription.unsubscribe();
    }

    public get channel(): FormControl {
        return this.formControl.get('channel') as FormControl;
    }

    public get currentPage(): Page<AuditEntity> | undefined {
        return this.fetchedPages.get(this._currentPage);
    }

    public get currentPageable(): Pageable {
        return this._currentPageable$.getValue();
    }

    public handleRangeChange(range: DaterangepickerType): void {
        this.range = range;
        this.fetchedPages.clear();
        this._currentPageable$.next(this.currentPageable.first());
    }

    public showMore(): void {
        this._currentPageable$.next(this.currentPageable.next());
    }

    public trackPage(index: number, entry: KeyValue<number, Page<AuditEntity>>): number {
        return entry.key;
    }

    public trackAuditEntity(index: number, item: AuditEntity) {
        return item.id;
    }
}
