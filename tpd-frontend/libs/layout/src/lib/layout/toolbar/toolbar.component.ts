import {
    AfterViewInit,
    ChangeDetectionStrategy,
    Component,
    ElementRef,
    HostBinding,
    Inject,
    Input,
    OnDestroy,
    OnInit,
    PLATFORM_ID,
    Renderer2,
} from '@angular/core';
import { StickyDirective } from '@devacfr/bootstrap';
import { ClassBuilder, getCSSVariableValue } from '@devacfr/util';
import { Subscription } from 'rxjs';

@Component({
    selector: 'lt-toolbar',
    templateUrl: './toolbar.component.html',
    styleUrls: ['./toolbar.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ToolbarComponent implements OnInit, AfterViewInit, OnDestroy {
    @Input()
    public sticky = false;

    @HostBinding('id')
    public id = 'lt_toolbar';

    @HostBinding('class')
    @Input()
    public get class(): string {
        const builder = ClassBuilder.create('pb-5 pb-lg-7 toolbar');
        if (this._class) builder.css(this._class);
        if (this._sticklyEnabled) builder.css('pt-5');
        return builder.toString();
    }

    public set class(value: string) {
        this._class = value;
    }

    @HostBinding('attr.ltSticky')
    private stickyDirective: StickyDirective;

    private _class = '';
    private _sticklyEnabled = false;
    private _subscription = new Subscription();

    constructor(
        private _elementRef: ElementRef,
        @Inject(PLATFORM_ID) private platformId: string,
        private _renderer: Renderer2
    ) {
        this.stickyDirective = new StickyDirective(this._elementRef, this.platformId);
        this.stickyDirective.marginTop = this.getMarginTop();
        this.stickyDirective.backgroundColor = 'var(--bs-body-bg)';
        this._subscription.add(
            this.stickyDirective.stickyChange.subscribe((status) => {
                const current = this._sticklyEnabled;
                this._sticklyEnabled = status;
                if (current !== status) {
                    if (status) {
                        this._renderer.addClass(this._elementRef.nativeElement, 'pt-5');
                    } else {
                        this._renderer.removeClass(this._elementRef.nativeElement, 'pt-5');
                    }
                }
            })
        );
    }

    ngOnInit() {
        if (this.sticky) {
            this.stickyDirective.ngOnInit();
        }
    }

    ngAfterViewInit(): void {
        if (this.sticky) {
            this.stickyDirective.ngAfterViewInit();
        }
    }

    public ngOnDestroy(): void {
        this._subscription.unsubscribe();
        if (this.sticky) {
            this.stickyDirective.ngOnDestroy();
        }
    }

    private getMarginTop(): number {
        return parseInt(getCSSVariableValue('--lt-header-height-desktop'));
    }
}
