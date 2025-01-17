import { isPlatformBrowser } from '@angular/common';
import {
    AfterViewInit,
    Directive,
    ElementRef,
    EventEmitter,
    HostBinding,
    HostListener,
    Inject,
    Input,
    isDevMode,
    OnDestroy,
    OnInit,
    Output,
    PLATFORM_ID,
} from '@angular/core';
import { animationFrameScheduler, BehaviorSubject, combineLatest, Observable, Subject } from 'rxjs';
import { filter, map, share, startWith, takeUntil, throttleTime } from 'rxjs/operators';

/**
 * Extended version of "Sticky Directive for Angular 2+"
 * https://github.com/w11k/angular-sticky-things
 */

export interface StickyPositions {
    offsetY: number;
    bottomBoundary: number | null;
}

export interface StickyStatus {
    isSticky: boolean;
    reachedLowerEdge: boolean;
    marginTop: number;
    marginBottom: number;
}

@Directive({
    selector: '[ltSticky]',
})
export class StickyDirective implements OnInit, AfterViewInit, OnDestroy {
    @Input()
    public scrollContainer: string | HTMLElement | undefined;

    @Input()
    public backgroundColor = '#fff';

    @Input()
    public zIndex = 10;

    @Input()
    public spacerElement: HTMLElement | undefined;

    @Input()
    public boundaryElement: HTMLElement | undefined;

    @HostBinding('class.is-sticky')
    private sticky = false;

    @HostBinding('class.boundary-reached')
    private boundaryReached = false;

    private filterGate = false;
    private marginTop$ = new BehaviorSubject(0);
    private marginBottom$ = new BehaviorSubject(0);
    private enable$ = new BehaviorSubject(true);

    /**
     * The field represents some position values in normal (not sticky) mode.
     * If the browser size or the content of the page changes, this value must be recalculated.
     */
    private scroll$ = new Subject<number>();
    private scrollThrottled$: Observable<number>;
    private resize$ = new Subject<void>();
    private resizeThrottled$: Observable<unknown>;
    private extraordinaryChange$ = new BehaviorSubject<void>(undefined);
    private status$: Observable<StickyStatus>;
    private componentDestroyed = new Subject<void>();

    constructor(private stickyElement: ElementRef, @Inject(PLATFORM_ID) private platformId: string) {
        /** Throttle the scroll to animation frame (around 16.67ms) */
        this.scrollThrottled$ = this.scroll$.pipe(throttleTime(0, animationFrameScheduler), share());

        /** Throttle the resize to animation frame (around 16.67ms) */
        this.resizeThrottled$ = this.resize$.pipe(
            throttleTime(0, animationFrameScheduler),
            // emit once since we are currently using combineLatest
            startWith(null),
            share()
        );

        this.status$ = combineLatest([
            this.enable$,
            this.scrollThrottled$,
            this.marginTop$,
            this.marginBottom$,
            this.extraordinaryChange$,
            this.resizeThrottled$,
        ]).pipe(
            filter(([enabled]) => this.checkEnabled(enabled)),
            map(([enabled, pageYOffset, marginTop, marginBottom]) =>
                this.determineStatus(this.determineElementOffsets(), pageYOffset, marginTop, marginBottom, enabled)
            ),
            share()
        );
    }

    @Input() set marginTop(value: number) {
        this.marginTop$.next(value);
    }

    @Input() set marginBottom(value: number) {
        this.marginBottom$.next(value);
    }

    @Input() set enable(value: boolean) {
        this.enable$.next(value);
    }

    @Output()
    public stickyChange = new EventEmitter<boolean>(true);

    @Output()
    public stickyChanged = new EventEmitter<boolean>(true);

    ngAfterViewInit(): void {
        this.status$.pipe(takeUntil(this.componentDestroyed)).subscribe((status) => this.setSticky(status));
    }

    public recalculate(): void {
        if (isPlatformBrowser(this.platformId)) {
            // Make sure to be in the next tick by using timeout
            setTimeout(() => {
                this.extraordinaryChange$.next(undefined);
            }, 0);
        }
    }

    /**
     * This is nasty code that should be refactored at some point.
     *
     * The Problem is, we filter for enabled. So that the code doesn't run
     * if @Input enabled = false. But if the user disables, we need exactly 1
     * emit in order to reset and call removeSticky. So this method basically
     * turns the filter in "filter, but let the first pass".
     */
    checkEnabled(enabled: boolean): boolean {
        if (!isPlatformBrowser(this.platformId)) {
            return false;
        }

        if (enabled) {
            // reset the gate
            this.filterGate = false;
            return true;
        } else {
            if (this.filterGate) {
                // gate closed, first emit has happened
                return false;
            } else {
                // this is the first emit for enabled = false,
                // let it pass, and activate the gate
                // so the next wont pass.
                this.filterGate = true;
                return true;
            }
        }
    }

    @HostListener('window:resize', [])
    onWindowResize(): void {
        if (isPlatformBrowser(this.platformId)) {
            this.resize$.next();
        }
    }

    setupListener(): void {
        if (isPlatformBrowser(this.platformId)) {
            const target = this.getScrollTarget();
            if (target) target.addEventListener('scroll', this.listener);
        }
    }

    removeListener() {
        if (isPlatformBrowser(this.platformId)) {
            const target = this.getScrollTarget();
            if (target) target.removeEventListener('scroll', this.listener);
        }
    }

    listener = (e: Event) => {
        const upperScreenEdgeAt = (e.target as HTMLElement).scrollTop || window.pageYOffset;
        this.scroll$.next(upperScreenEdgeAt);
    };

    ngOnInit(): void {
        // this.checkSetup();
        this.setupListener();
    }

    ngOnDestroy(): void {
        this.componentDestroyed.next();
        this.removeListener();
    }

    getComputedStyle(el: HTMLElement): ClientRect | DOMRect {
        return el.getBoundingClientRect();
    }

    private getScrollTarget(): Element | Window | null {
        let target: Element | Window | null;

        if (this.scrollContainer && typeof this.scrollContainer === 'string') {
            target = document.querySelector(this.scrollContainer);
        } else if (this.scrollContainer && this.scrollContainer instanceof HTMLElement) {
            target = this.scrollContainer;
        } else {
            target = window;
        }
        return target;
    }

    private determineStatus(
        originalVals: StickyPositions,
        pageYOffset: number,
        marginTop: number,
        marginBottom: number,
        enabled: boolean
    ): StickyStatus {
        const stickyElementHeight = this.getComputedStyle(this.stickyElement.nativeElement).height;
        let reachedLowerEdge = false;
        if (originalVals.bottomBoundary != null) {
            reachedLowerEdge =
                this.boundaryElement != null &&
                window.pageYOffset + stickyElementHeight + marginBottom >= originalVals.bottomBoundary - marginTop;
        }
        return {
            isSticky: enabled && pageYOffset > originalVals.offsetY,
            reachedLowerEdge,
            marginBottom,
            marginTop,
        };
    }

    /**
     * Gets the offset for element. If the element
     * currently is sticky, it will get removed
     * to access the original position. Other
     * wise this would just be 0 for fixed elements.
     */
    private determineElementOffsets(): StickyPositions {
        if (this.sticky) {
            this.removeSticky();
        }

        let bottomBoundary: number | null = null;

        if (this.boundaryElement) {
            const boundaryElementHeight = this.getComputedStyle(this.boundaryElement).height;
            const boundaryElementOffset = getPosition(this.boundaryElement).y;
            bottomBoundary = boundaryElementHeight + boundaryElementOffset;
        }

        return {
            offsetY: getPosition(this.stickyElement.nativeElement).y - this.marginTop$.value,
            bottomBoundary,
        };
    }

    private makeSticky(boundaryReached = false, marginTop: number, marginBottom: number): void {
        this.boundaryReached = boundaryReached;
        this.sticky = true;
        // do this before setting it to pos:fixed
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        const { width, height, left } = this.getComputedStyle(this.stickyElement.nativeElement);
        let offSet = 0;
        if (this.boundaryElement) {
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            offSet = boundaryReached
                ? this.getComputedStyle(this.boundaryElement).bottom - height - this.marginBottom$.value
                : this.marginTop$.value;
        }

        this.stickyElement.nativeElement.style.position = 'sticky';
        this.stickyElement.nativeElement.style.backgroundColor = this.backgroundColor;
        this.stickyElement.nativeElement.style.top = this.marginTop$.value + 'px';
        // this.stickyElement.nativeElement.style.left = left + 'px';
        this.stickyElement.nativeElement.style.width = `${width}px`;
        this.stickyElement.nativeElement.style.zIndex = this.zIndex;
        if (this.spacerElement) {
            const spacerHeight = marginBottom + height + marginTop;
            this.spacerElement.style.height = `${spacerHeight}px`;
        }
    }

    private checkSetup() {
        if (isDevMode() && !this.spacerElement) {
            console.warn(`******There might be an issue with your sticky directive!******

You haven't specified a spacer element. This will cause the page to jump.

Best practise is to provide a spacer element (e.g. a div) right before/after the sticky element.
Then pass the spacer element as input:

<div #spacer></div>

<div stickyThing="" [spacer]="spacer">
    I am sticky!
</div>`);
        }
    }

    private setSticky(status: StickyStatus): void {
        this.stickyChange.emit(status.isSticky);
        if (status.isSticky) {
            this.makeSticky(status.reachedLowerEdge, status.marginTop, status.marginBottom);
        } else {
            this.removeSticky();
        }
        this.stickyChanged.emit(status.isSticky);
    }

    private removeSticky(): void {
        this.boundaryReached = false;
        this.sticky = false;

        this.stickyElement.nativeElement.style.position = '';
        this.stickyElement.nativeElement.style.backgroundColor = 'inherit';
        this.stickyElement.nativeElement.style.width = 'auto';
        this.stickyElement.nativeElement.style.left = 'auto';
        this.stickyElement.nativeElement.style.top = 'auto';
        this.stickyElement.nativeElement.style.zIndex = 'auto';
        if (this.spacerElement) {
            this.spacerElement.style.height = '0';
        }
    }
}

// Thanks to https://stanko.github.io/javascript-get-element-offset/
function getPosition(el: HTMLElement) {
    let top = 0;
    let left = 0;
    let element = el;

    // Loop through the DOM tree
    // and add it's parent's offset to get page offset
    do {
        top += element.offsetTop || 0;
        left += element.offsetLeft || 0;
        element = element.offsetParent as HTMLElement;
    } while (element);

    return {
        y: top,
        x: left,
    };
}
