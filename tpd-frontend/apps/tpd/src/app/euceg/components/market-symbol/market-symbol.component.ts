import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    HostBinding,
    Input,
    OnChanges,
    OnInit,
    SimpleChanges,
} from '@angular/core';
import { ClassBuilder } from '@devacfr/util';
import { EMPTY, Observable, ReplaySubject } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { MarketSymbolValue } from './typing';

@Component({
    selector: 'app-market-symbol',
    templateUrl: './market-symbol.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MarketSymbolComponent implements OnInit, OnChanges {
    @HostBinding('class')
    public get class() {
        const builder = ClassBuilder.create(this._class);
        if (this.hover) builder.css('symbol-hover');
        return builder.toString();
    }

    @Input()
    public countries: MarketSymbolValue[] | ((obs: Observable<unknown>) => Observable<MarketSymbolValue[]>) | undefined;

    @Input()
    public size = 6;

    @Input()
    public hover = false;

    @Input()
    public total = 0;

    public countries$: Observable<MarketSymbolValue[]> | undefined;
    private _countriesSubject = new ReplaySubject<unknown>();

    private _class = 'symbol-group';
    constructor(private _cd: ChangeDetectorRef) {}

    ngOnInit(): void {
        this.countries$ = this.createObserver();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.countries && changes.countries.currentValue !== changes.countries.previousValue) {
            if (changes.countries.firstChange) {
                this._countriesSubject.next(changes.countries.currentValue);
            } else if (typeof changes.countries.currentValue !== 'function') {
                this._countriesSubject.next(changes.countries.currentValue);
            }
        }
    }

    public get nextTotal(): number {
        return this.total - this.size;
    }

    private createObserver(): Observable<MarketSymbolValue[]> {
        return this._countriesSubject.pipe(
            (obs: Observable<unknown>) => {
                if (!this.countries) return EMPTY;
                if (typeof this.countries !== 'function') {
                    return obs.pipe(map(() => this.countries as MarketSymbolValue[]));
                } else {
                    return this.countries(obs);
                }
            },
            tap((countries) => {
                this.total = countries.length;
                this._cd.markForCheck();
            }),
            map((countries) => countries.slice(0, this.size))
        );
    }
}
