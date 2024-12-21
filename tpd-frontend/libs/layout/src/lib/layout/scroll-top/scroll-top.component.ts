import { Component, ElementRef, HostBinding, HostListener, OnDestroy, Renderer2 } from '@angular/core';
import { NavigationCancel, NavigationEnd, Router } from '@angular/router';
import { ElementAnimateUtil, getScrollTop } from '@devacfr/util';
import { fromEvent, Subscription } from 'rxjs';
import { throttleTime } from 'rxjs/operators';

export interface ScrollTopOptions {
    offset: number;
    speed: number;
}

export const DefaultScrollTopOptions: ScrollTopOptions = {
    offset: 200,
    speed: 600,
};

@Component({
    selector: 'lt-scroll-top',
    templateUrl: './scroll-top.component.html',
})
export class LayoutScrollTopComponent implements OnDestroy {
    @HostBinding('class')
    public class = 'scrolltop';
    @HostBinding('id')
    public id = 'lt_scrolltop';

    private options: ScrollTopOptions;

    private _subscriptions = new Subscription();

    constructor(private _element: ElementRef, private _renderer: Renderer2, private _router: Router) {
        this.options = Object.assign({}, DefaultScrollTopOptions);
        this._subscriptions.add(
            this._router.events.subscribe((event) => {
                if (event instanceof NavigationEnd || event instanceof NavigationCancel) {
                    setTimeout(() => {
                        this.scrollTop();
                    }, 0);
                }
            })
        );
        this._subscriptions.add(
            fromEvent(window, 'scroll')
                .pipe(throttleTime(50))
                .subscribe(() => this.scroll())
        );
    }

    @HostListener('click', ['$event'])
    public handleClick(event: Event) {
        event.preventDefault();
        this.go();
    }

    private scroll() {
        const offset = this.options.offset;
        const pos = getScrollTop(); // current vertical position
        if (pos > offset) {
            if (!document.body.hasAttribute('data-lt-scrolltop')) {
                this._renderer.setAttribute(document.body, 'data-lt-scrolltop', 'on');
            }
        } else {
            if (document.body.hasAttribute('data-lt-scrolltop')) {
                this._renderer.removeAttribute(document.body, 'data-lt-scrolltop');
            }
        }
    }

    public go() {
        const speed = this.options.speed;
        ElementAnimateUtil.scrollTop(0, speed);
    }

    scrollTop() {
        ElementAnimateUtil.scrollTop(0, DefaultScrollTopOptions.speed);
    }

    ngOnDestroy() {
        this._subscriptions.unsubscribe();
    }
}
