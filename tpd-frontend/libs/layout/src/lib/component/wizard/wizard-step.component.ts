import {
    Component,
    ElementRef,
    EventEmitter,
    HostBinding,
    Input,
    NgZone,
    OnDestroy,
    OnInit,
    Output,
} from '@angular/core';
import { WizardComponent } from './wizard.component';

@Component({
    selector: 'lt-wizard-step',
    exportAs: 'wizardStep',
    template: ` <ng-content></ng-content> `,
})
export class WizardStepComponent implements OnInit, OnDestroy {
    public index = 0;

    public wizard: WizardComponent | undefined;

    private _isActive = false;

    private observer!: IntersectionObserver;

    @HostBinding('attr.data-wizard-type')
    public type = 'step-content';

    @HostBinding('attr.data-wizard-state')
    public get state(): string {
        return this.isActive ? 'current' : '';
    }

    @Output()
    public beforePrevious: EventEmitter<any> = new EventEmitter<any>();

    @Output()
    public visibility = new EventEmitter<boolean>();

    @Output()
    public activate = new EventEmitter<boolean>();

    @Input()
    public forwardAccepted = true;

    @Input()
    public ordering = 0;

    @Input()
    public set isActive(isActive: boolean) {
        this._isActive = isActive;
        this.activate.emit(this._isActive);
    }

    public get isActive(): boolean {
        return this._isActive;
    }

    public get isValid(): boolean {
        return this.valid(this);
    }

    @Input()
    public title: string | undefined;

    @Input()
    public description: string | undefined;

    @Input()
    public icon: string | undefined;

    @Input()
    public valid: (step: WizardStepComponent) => boolean = () => true;

    @Input()
    public beforeNext: (() => boolean) | (() => Promise<boolean>) = () => true;

    @Input()
    public beforeComplete: (() => boolean) | (() => Promise<boolean>) = () => true;

    constructor(private el: ElementRef<HTMLElement>, private ngZone: NgZone) {}

    ngOnInit(): void {
        this.ngZone.runOutsideAngular(() => {
            this.observer = new IntersectionObserver((entries) => {
                entries.forEach((e) => {
                    this.visibility.emit(e.isIntersecting);
                });
            });
            this.observer.observe(this.el.nativeElement);
        });
    }

    ngOnDestroy(): void {
        this.observer.disconnect();
    }
}
