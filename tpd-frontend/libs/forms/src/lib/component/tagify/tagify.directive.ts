import {
    AfterContentInit,
    Directive,
    ElementRef,
    EventEmitter,
    HostBinding,
    Input,
    OnChanges,
    OnDestroy,
    Optional,
    Output,
    Self,
    SimpleChanges,
} from '@angular/core';
import { ControlValueAccessor, NgControl } from '@angular/forms';
import Tagify, { TagifySettings } from '@yaireo/tagify';
import { Observable, Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, tap } from 'rxjs/operators';
import { WhiteListObserver, WhiteListResult } from '.';

// eslint-disable-next-line @angular-eslint/directive-selector
@Directive({ selector: '[tagify]', exportAs: 'tagify' })
export class TagifyDirective implements AfterContentInit, OnChanges, OnDestroy, ControlValueAccessor {
    @HostBinding('class')
    public class = 'tagify';

    @Input()
    public readonly = false;

    @Input()
    public userInput = true;

    /**
     * If `true`, do not add invalid, temporary tags before automatically
     */
    @Input()
    public skipInvalid = false;

    /**
     * `TagData` property which will be displayed as the tag's text.
     */
    @Input()
    public tagTextProp: string | undefined;

    /**
     * Options for offering a dropdown menu with available tags.
     */
    @Input()
    public dropdown: Tagify.DropDownSettings | undefined;

    /**
     * Should ONLY use tags allowed in whitelist.
     * In `mix` mode, setting it to `false` will not allow creating new tags.
     */
    @Input()
    public enforceWhitelist = false;

    @Input()
    public searchDelay = 300;

    /**
     * An array of allowed tags.
     */
    @Input()
    public whitelist: string[] | Tagify.TagData[] | WhiteListObserver | undefined;

    /**
     * An array of tags which aren't allowed.
     * @default Empty array.
     */
    @Input()
    public blacklist?: string[] | undefined;

    /**
     * Template for the rendered tags.
     */
    @Input()
    public tagTemplate: ((this: Tagify, item: Tagify.TagData) => string) | undefined;

    /**
     * This callback is called once for each item in the dropdown list. It
     * is given an item and should return the HTML markup for that item.
     */
    @Input()
    public dropdownItemTemplate: ((this: Tagify, item: Tagify.TagData) => string) | undefined;

    /**
     * A tag has been added.
     */
    @Output()
    public add = new EventEmitter<CustomEvent<Tagify.AddEventData>>();

    /**
     * A tag has been removed
     */
    @Output()
    public remove = new EventEmitter<CustomEvent<Tagify.RemoveEventData>>();

    /**
     * Suggestions dropdown is about to be rendered. The dropdown DOM node
     * is passed to the callback.
     */
    @Output()
    public showDropdown = new EventEmitter<CustomEvent<Tagify.DropDownShowEventData>>();

    /**
     * A suggestions dropdown item got selected (by mouse / keyboard /
     * touch).
     */
    @Output()
    public selectDropdown = new EventEmitter<CustomEvent<Tagify.DropDownSelectEventData>>();

    /**
     * When the dropdown menu is open and its items were recomputed via
     * `Tagify.refilter`.
     */
    @Output()
    public updatedDropdown = new EventEmitter<CustomEvent<Tagify.DropDownUpdatedEventData>>();

    @Output() valueChange = new EventEmitter<Tagify.TagData[] | undefined>();

    @Output()
    public inputChange = new EventEmitter<CustomEvent<Tagify.InputEventData>>();

    protected _disabled = false;

    private _inputEventEmitter = new Subject<string>();
    protected _whitelist$: Observable<WhiteListResult> | undefined;
    private _tagify: Tagify | undefined;
    private _value: Tagify.TagData[] | undefined = undefined;
    private _subscriptions = new Subscription();
    public async = false;

    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    private _onChange = (_: unknown) => {
        //noop
    };

    // eslint-disable-next-line @typescript-eslint/no-empty-function
    private _onTouched = () => {};

    constructor(@Self() @Optional() private _ngControl: NgControl, private _element: ElementRef) {
        if (this._ngControl) {
            this._ngControl.valueAccessor = this;
        }
    }
    ngAfterContentInit(): void {
        this.async = false;
        if (typeof this.whitelist === 'function') {
            this.async = true;
        }

        if (this.whitelist && this.async) {
            this.asyncActions();
        }

        this._tagify = new Tagify(this._element.nativeElement, this.getSetttings());
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.readonly && changes.readonly.currentValue !== changes.readonly.previousValue) {
            this._tagify?.setReadonly(this.readonly);
        }
    }
    ngOnDestroy(): void {
        this._subscriptions.unsubscribe();
    }

    protected asyncActions(): void {
        const ds = this.whitelist as WhiteListObserver;
        this._whitelist$ = this._inputEventEmitter.pipe(
            debounceTime(this.searchDelay),
            distinctUntilChanged(),
            tap(() => {
                if (this._tagify) {
                    this._tagify.loading(true);
                }
            }),
            ds
        );
        this._subscriptions.add(
            this._whitelist$.subscribe((result: WhiteListResult) => {
                if (this._tagify) {
                    this._tagify.whitelist = result.data; // update whitelist Array in-place
                    this._tagify.loading(false).dropdown.show(result.searchTerm); // render the suggestions dropdown}
                }
            })
        );
        this._subscriptions.add(
            this.inputChange.subscribe((data) => {
                const tag = data.detail;
                if ('value' in tag) {
                    this._inputEventEmitter.next(tag.value);
                } else if ('textContent' in tag) {
                    this._inputEventEmitter.next(tag.textContent);
                }
            })
        );
    }

    private getSetttings(): TagifySettings {
        const templates: Tagify.Templates = {};
        if (this.tagTemplate) templates.tag = this.tagTemplate;
        if (this.dropdownItemTemplate) templates.dropdownItem = this.dropdownItemTemplate;
        return {
            userInput: this.userInput,
            tagTextProp: this.tagTextProp,
            dropdown: this.dropdown ? this.dropdown : {},
            skipInvalid: this.skipInvalid,
            enforceWhitelist: this.enforceWhitelist,
            whitelist: this.whitelist && !(typeof this.whitelist === 'function') ? this.whitelist : [],
            blacklist: this.blacklist ? this.blacklist : [],
            templates: templates,
            callbacks: {
                input: (event: CustomEvent<Tagify.InputEventData<Tagify.TagData>>) => this.inputChange.emit(event),
                add: (event: CustomEvent<Tagify.AddEventData<Tagify.TagData>>) => this.onAdd(event),
                remove: (event: CustomEvent<Tagify.RemoveEventData<Tagify.TagData>>) => this.onRemove(event),
                blur: () => this.onBlur(),
                change: (event: CustomEvent<Tagify.ChangeEventData<Tagify.TagData>>) => this.onChange(event),
                'dropdown:select': (event: CustomEvent<Tagify.DropDownSelectEventData<Tagify.TagData>>) =>
                    this.selectDropdown.emit(event),
                'dropdown:show': (event: CustomEvent<Tagify.DropDownShowEventData<Tagify.TagData>>) =>
                    this.showDropdown.emit(event),
                'dropdown:updated': (event: CustomEvent<Tagify.DropDownUpdatedEventData<Tagify.TagData>>) =>
                    this.updatedDropdown.emit(event),
            },
        };
    }
    /**
     *
     */
    public get value(): Tagify.TagData[] | undefined {
        return this._value;
    }

    /**
     *
     */
    @Input()
    public set value(value: Tagify.TagData[] | undefined) {
        if (this.value !== value) {
            this.writeValue(value);
        }
    }

    /**
     *
     */
    onBlur(): void {
        this.markAsTouched();
    }

    private onAdd(event: CustomEvent<Tagify.AddEventData<Tagify.TagData>>): void {
        this.onChange(event);
        this.add.emit(event);
    }

    private onRemove(event: CustomEvent<Tagify.RemoveEventData<Tagify.TagData>>): void {
        this.onChange(event);
        this.remove.emit(event);
    }

    /**
     *
     */
    private onChange(event: CustomEvent<Tagify.EventData<Tagify.TagData>>): void {
        this._value = event.detail.tagify.value;
        this.markAsChanged();
        this.markAsTouched();
        this.valueChange.emit(this._value);
    }

    /**
     *
     */
    public get disabled(): boolean {
        return this._disabled;
    }

    /**
     *
     * @param isDisabled
     */
    public setDisabledState?(isDisabled: boolean): void {
        this._disabled = isDisabled;
    }

    writeValue(value: string | string[] | Tagify.TagData[] | undefined): void {
        if (!this._tagify) return;
        this._value = this.convertToTagData(value);
        this._tagify.removeAllTags({
            withoutChangeEvent: true,
        });
        this._tagify.addTags(this._value, true, true);
    }

    /**
     *
     * @param fn
     */
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    registerOnChange(fn: any): void {
        this._onChange = fn;
    }
    /**
     *
     * @param fn
     */
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    registerOnTouched(fn: any): void {
        this._onTouched = fn;
    }

    /**
     *
     */
    public markAsTouched() {
        this._onTouched();
    }

    /**
     *
     */
    public markAsChanged() {
        this._onChange(this.value);
    }

    private convertToTagData(value: string | string[] | Tagify.TagData[] | undefined): Tagify.TagData[] {
        if (!value) return [];
        let ar;
        if (typeof value === 'string') {
            ar = value.split(',');
        } else {
            ar = value;
        }
        return ar.map((value) => (typeof value === 'string' ? { value } : value));
    }
}
