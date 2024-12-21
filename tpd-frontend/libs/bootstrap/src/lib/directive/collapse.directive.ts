import {
    AfterContentInit,
    Directive,
    ElementRef,
    EventEmitter,
    HostBinding,
    Input,
    OnChanges,
    OnDestroy,
    Output,
    Renderer2,
    SimpleChanges,
} from '@angular/core';
import { Collapse } from 'bootstrap';

@Directive({
    selector: '[ltCollapse]',
    exportAs: 'ltCollapse',
})
export class CollapseDirective implements AfterContentInit, OnChanges, OnDestroy {
    @Input()
    public set ltCollapse(value: boolean) {
        this._ltCollapse = value;
        this.collapse.emit(this._ltCollapse);
    }

    @Input()
    public parentContainer: string | Element | JQuery<HTMLElement> | undefined;

    @Input()
    public targetContainer: string | Element | JQuery<HTMLElement> | undefined;

    @Output()
    public showTarget = new EventEmitter<Event>();

    @Output()
    public shown = new EventEmitter<Event>();

    @Output()
    public hideTarget = new EventEmitter<Event>();

    @Output()
    public hidden = new EventEmitter<Event>();

    @Output()
    public collapse = new EventEmitter<boolean>();

    public get collapsed() {
        return this._collapsed;
    }

    @HostBinding('class')
    protected _class = 'collapse';

    private _bsElement: Collapse | undefined;
    private _collapsed = true;
    private _ltCollapse = true;

    private _unlistener: (() => void)[] = [];

    constructor(private _element: ElementRef, private _renderer: Renderer2) {}

    ngAfterContentInit(): void {
        this._bsElement = this.getOrcreateCollapse();
    }

    ngOnChanges(changes: SimpleChanges): void {
        let toUpdate = false;
        if (changes.parentContainer && changes.parentContainer.currentValue !== changes.parentContainer.previousValue) {
            toUpdate = true;
        }
        if (changes.targetContainer && changes.targetContainer.currentValue !== changes.targetContainer.previousValue) {
            toUpdate = true;
        }
        if (toUpdate) {
            this._bsElement = this.getOrcreateCollapse(true);
        }
    }

    ngOnDestroy(): void {
        this._unlistener.forEach((unlistener) => unlistener());
        this._bsElement?.dispose();
    }

    public show(): void {
        this._bsElement?.show();
    }

    public hide(): void {
        this._bsElement?.hide();
    }

    public toggle(): void {
        this._bsElement?.toggle();
    }

    private getOrcreateCollapse(force = false): Collapse {
        if (this._bsElement && force) {
            this._unlistener.forEach((unlistener) => unlistener());
            this._unlistener = [];
            this._bsElement.dispose();
        }
        const collapse = new Collapse(this.targetContainer ? this.targetContainer : this._element.nativeElement, {
            toggle: this._ltCollapse,
            parent: this.parentContainer,
        });
        this._unlistener.push(
            this._renderer.listen(this._element.nativeElement, 'show.bs.collapse', (event) => {
                this.showTarget.emit(event);
            })
        );
        this._unlistener.push(
            this._renderer.listen(this._element.nativeElement, 'shown.bs.collapse', (event) => {
                this._collapsed = false;
                this.collapse.emit(this._collapsed);
                this.shown.emit(event);
            })
        );
        this._unlistener.push(
            this._renderer.listen(this._element.nativeElement, 'hide.bs.collapse', (event) => {
                this.hideTarget.emit(event);
            })
        );
        this._unlistener.push(
            this._renderer.listen(this._element.nativeElement, 'hidden.bs.collapse', (event) => {
                this._collapsed = true;
                this.collapse.emit(this._collapsed);
                this.hidden.emit(event);
            })
        );

        return collapse;
    }
}
