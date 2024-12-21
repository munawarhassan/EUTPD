import {
    AfterContentInit,
    Directive,
    ElementRef,
    EventEmitter,
    Input,
    OnChanges,
    OnDestroy,
    Optional,
    Output,
    Renderer2,
    Self,
    SimpleChanges,
} from '@angular/core';
import { ControlValueAccessor, NgControl } from '@angular/forms';
import _, { isArray } from 'lodash-es';
import { EMPTY, isObservable, Observable, of, Subject, Subscription } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, switchMap, tap } from 'rxjs/operators';
import {
    DataFormat,
    GroupedDataFormat,
    IdTextPair,
    OptionData,
    Options,
    ProcessedResult,
    SearchOptions,
} from 'select2';
import { Select2Data, Select2Event, Select2Observer, Select2Value } from '.';
import { synchronizeStatusChange } from '../shared';

@Directive({
    // eslint-disable-next-line @angular-eslint/directive-selector
    selector: 'select[select2]',
    exportAs: 'select2',
})
export class Select2Directive implements OnChanges, AfterContentInit, OnDestroy, ControlValueAccessor {
    @Input()
    public mode: 'single' | 'multi' = 'single';

    @Input()
    public searchDelay = 300;

    @Input()
    public optionValue: string | undefined;

    @Input()
    public optionText: string | undefined;

    @Input()
    public allowClear = false;

    @Input()
    public dropdownParent: string | undefined;

    @Input()
    public placeholder: string | IdTextPair | undefined;

    @Input()
    public hideSearch = true;

    /**
     * Dynamically create new options from text input by the user in the search box or directly in input element.
     */
    @Input()
    public tags:
        | {
              tokenSeparators?: string[];
              createTag?: ((params: SearchOptions) => (IdTextPair & Record<string, unknown>) | null) | undefined;
              insertTag?: ((data: Array<OptionData | IdTextPair>, tag: IdTextPair) => void) | undefined;
          }
        | undefined;

    @Input()
    public datasource: Select2Data | Select2Observer<Select2Data> | Observable<Select2Data> | undefined | null;

    public async = false;

    public _disabled = false;

    @Output()
    public valueChange = new EventEmitter<string | (string | undefined)[] | undefined>();

    private _selected: string | (string | undefined)[] | undefined;

    protected _eventEmitter = new Subject<Select2Event>();
    protected _datasource$: Observable<Select2Data> | undefined;
    private _successAsyncCallback: (data: unknown) => void = _.noop;
    private _failureAsyncCallback = _.noop;
    private _state: 'initial' | 'prefetch' | 'search' = 'initial';

    private _subscriptions = new Subscription();

    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    private _onChange = (_: unknown) => {
        //noop
    };

    // eslint-disable-next-line @typescript-eslint/no-empty-function
    private _onTouched = () => {};

    constructor(
        @Self() @Optional() private _ngControl: NgControl,
        private _element: ElementRef,
        private _renderer: Renderer2
    ) {
        const select2 = $(this._element.nativeElement);
        select2.on('select2:select', () => this.handleValueChange());
        select2.on('select2:clear', () => this.handleValueChange());
        if (this._ngControl) {
            this._ngControl.valueAccessor = this;
            this._subscriptions.add(
                this._ngControl.control?.statusChanges?.subscribe(() => {
                    this.synchronizeStatusChange(this._element);
                })
            );
        }
    }
    ngAfterContentInit(): void {
        this.async = false;
        if (typeof this.datasource === 'function') {
            this.async = true;
        }

        if (this.datasource) {
            if (this.async) {
                this.asyncActions();
            } else {
                this.syncActions();
                this._eventEmitter.next({
                    state: 'initial',
                });
            }
        } else {
            $(this._element.nativeElement).select2(this.getOptions());
        }
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (
            changes.datasource &&
            !changes.datasource.firstChange &&
            !(typeof changes.datasource.currentValue === 'function')
        ) {
            this.finalizeSyncCall(changes.datasource.currentValue);
        }
    }

    ngOnDestroy(): void {
        this._subscriptions.unsubscribe();
        this.destroySelect2();
    }

    public get value(): string | (string | undefined)[] | undefined {
        return this._selected;
    }

    public set value(value: string | (string | undefined)[] | undefined) {
        this._selected = value;
    }

    public handleValueChange() {
        const select2 = $(this._element.nativeElement);
        const val = select2.select2('data');
        const vals = val.map((v) => (v.id !== '' ? v.id : undefined));
        this._selected = this.mode === 'multi' ? vals : vals[0];
        this.markAsTouched();
        this.markAsChanged();
    }

    public getOptions(): Options {
        const options: Options = {};
        options.allowClear = this.allowClear;
        if (this.placeholder) options.placeholder = this.placeholder;
        if (this.hideSearch) options.minimumResultsForSearch = Infinity;
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        if (this.dropdownParent) {
            options.dropdownParent = $(this.dropdownParent);
        }
        options.multiple = this.mode === 'multi';
        if (this.tags) {
            options.tags = true;
            if (this.tags.createTag) options.createTag = this.tags.createTag;
            if (this.tags.insertTag) options.insertTag = this.tags.insertTag;
        }
        if (this.async && !this.hideSearch) {
            options.ajax = {
                processResults: (data: Select2Data): ProcessedResult => {
                    const ar = isArray(data) ? data : [data];
                    return {
                        results: ar.map((v) => this.convertToDataFormat(v)),
                    };
                },
                // eslint-disable-next-line @typescript-eslint/no-unused-vars
                transport: this.triggerAsyncSearch.bind(this),
            };
        }
        return options;
    }

    public setDisabledState?(isDisabled: boolean): void {
        this._disabled = isDisabled;
    }

    public get disabled(): boolean {
        return this._disabled;
    }

    public writeValue(value: Select2Data | string | string[] | undefined): void {
        const select2 = $(this._element.nativeElement);
        this._selected = this.convertFrom(value);
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        select2.val(this._selected as any).trigger('change');

        this.triggerAsyncPrefetch();
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    public registerOnChange(fn: any): void {
        this._onChange = fn;
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    public registerOnTouched(fn: any): void {
        this._onTouched = fn;
    }

    public markAsTouched() {
        this._onTouched();
    }

    public markAsChanged() {
        this._onChange(this._selected);
        this.valueChange.emit(this._selected);
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    private getOptionValue(value: Record<string, unknown>): string | undefined {
        return this.optionValue ? String(value[this.optionValue]) : undefined;
    }
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    private getOptionText(value: Record<string, unknown>): string | undefined {
        return this.optionText ? String(value[this.optionText]) : undefined;
    }

    protected asyncActions(): void {
        const select2 = $(this._element.nativeElement);
        select2.select2(this.getOptions());
        const ds = this.datasource as Select2Observer<Select2Value>;
        this._datasource$ = this._eventEmitter.pipe(
            debounceTime(this.searchDelay),
            distinctUntilChanged(),
            ds,
            tap((result) => this._successAsyncCallback(result)),
            catchError(() => {
                this._failureAsyncCallback();
                return EMPTY;
            })
        );
        this._subscriptions.add(
            this._datasource$.subscribe((result: Select2Data) => {
                if (this._state === 'prefetch') {
                    this.applyPrefetch(result);
                } else if (this._state === 'initial' && this.hideSearch) {
                    this.finalizeSyncCall(result);
                }
            })
        );
    }

    protected syncActions(): void {
        this._subscriptions.add(
            this._eventEmitter
                .pipe(
                    debounceTime(this.searchDelay),
                    switchMap(() => {
                        if (!this.datasource) return EMPTY;
                        if (isObservable(this.datasource)) {
                            return this.datasource;
                        } else {
                            const ar = isArray(this.datasource)
                                ? (this.datasource as Select2Value[])
                                : [this.datasource as Select2Value];
                            return of(ar);
                        }
                    })
                )
                .subscribe(this.finalizeSyncCall.bind(this))
        );
    }

    protected finalizeSyncCall(result: Select2Data): void {
        const options = this.getOptions();
        const ar = isArray(result) ? result.map(this.convertToDataFormat) : [this.convertToDataFormat(result)];
        // insert empty option for placeholder
        if (this.placeholder && this.mode === 'single') {
            ar.unshift({ id: '', text: '' } as DataFormat);
        }
        options.data = ar;
        this.destroySelect2();
        if (this.datasource != null) {
            $(this._element.nativeElement).find('option').remove();
        }
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        $(this._element.nativeElement)
            .select2(options)
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            .val(this._selected as any)
            .trigger('change');
    }

    private destroySelect2(): void {
        const select2 = $(this._element.nativeElement);
        if (select2.hasClass('select2-hidden-accessible')) {
            select2.select2('destroy');
        }
    }

    private triggerAsyncSearch(settings: JQueryAjaxSettings, success?: (data: unknown) => void, failure?: () => void) {
        if (success && failure) {
            this._successAsyncCallback = success;
            this._failureAsyncCallback = failure;
            let term;
            if (typeof settings.data == 'string') {
                term = settings.data;
            } else {
                term = settings.data?.term;
            }
            this.trigger({
                state: 'search',
                termMatch: term,
            });
        }
    }

    private triggerAsyncPrefetch(): void {
        if (!this.async) {
            return;
        }
        let selected: string[] = [];
        if (this._selected) {
            const ar = isArray(this._selected) ? this._selected : [this._selected];
            selected = ar.filter((v) => v != null).map((v) => String(v)) as string[];
        }
        this.trigger({
            state: 'prefetch',
            selected,
        });
    }

    private applyPrefetch(result: Select2Data) {
        const ar = isArray(result) ? result.map(this.convertToDataFormat) : [this.convertToDataFormat(result)];
        const el = this._element.nativeElement as HTMLSelectElement;
        // remove all child
        while (el.firstChild) {
            el.removeChild(el.firstChild);
        }
        // create selected
        ar.forEach((v) => {
            const opt = this._renderer.createElement('option') as HTMLOptionElement;
            opt.value = String(v.id);
            opt.text = v.text;
            opt.selected = true;
            this._renderer.appendChild(el, opt);
        });
    }

    private trigger(event: Select2Event) {
        this._state = event.state;
        this._eventEmitter.next(event);
    }

    private convertFrom(value: Select2Data | undefined): string | string[] | undefined {
        if (value == null) {
            return undefined;
        }
        const val = isArray(value) ? value : [value];

        const ar = val.filter((v) => v != null).map(this.convertToId) as string[];
        if (this.mode === 'single') {
            return ar[0];
        }
        return ar;
    }

    private convertToId = (value: Select2Data): string | undefined => {
        if (typeof value === 'number' || typeof value === 'string') return String(value);
        const r = value as Partial<DataFormat | GroupedDataFormat>;
        if (r.id && r.text) {
            return String(r.id);
        }
        return this.getOptionValue(value as Record<string, unknown>);
    };

    private convertToDataFormat = (value: Select2Value): DataFormat => {
        if (typeof value === 'number' || typeof value === 'string') {
            return {
                id: value,
                text: String(value),
            };
        }
        const r = value as DataFormat;
        if (r.id && r.text) {
            return r;
        }
        const id = this.getOptionValue(value as Record<string, unknown>) || '';
        const text = this.getOptionText(value as Record<string, unknown>) || '';
        return {
            id,
            text,
        };
    };

    public synchronizeStatusChange(elementRef: ElementRef) {
        setTimeout(() => {
            const el = this._element.nativeElement as HTMLElement;
            const select2 = el.nextElementSibling;
            if (select2) {
                const selection = select2.querySelector('.select2-selection') as HTMLElement;
                synchronizeStatusChange(elementRef, selection);
            }
        });
    }
}
