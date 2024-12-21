import {
    AfterViewInit,
    ChangeDetectorRef,
    Component,
    ContentChildren,
    ElementRef,
    EventEmitter,
    Input,
    OnInit,
    Output,
    QueryList,
    ViewChild,
    ViewEncapsulation,
} from '@angular/core';
import { Observable, of } from 'rxjs';
import { delay, map } from 'rxjs/operators';
import { WizardMode } from './typing';
import { WizardStepComponent } from './wizard-step.component';

export interface WizardEvent {
    target: WizardComponent;
}

@Component({
    selector: 'lt-wizard',
    exportAs: 'wizard',
    templateUrl: './wizard.component.html',
    styleUrls: ['wizard-aside.component.scss', 'wizard-linear.component.scss', 'wizard-tab.component.scss'],
    encapsulation: ViewEncapsulation.None,
})
export class WizardComponent implements OnInit, AfterViewInit {
    @Input()
    public mode: WizardMode = 'tab';

    @Input()
    public startStep = 1;

    @Input()
    public manualStepForward = true;

    @Input()
    public validateAtTheEnd = true;

    @Input()
    public previousButtonText = 'Previous';

    @Input()
    public nextButtonText = 'Next';

    @Input()
    public submitButtonText = 'Complete';

    @Output()
    public beforeNext = new EventEmitter<WizardEvent>();

    @Output()
    public beforePrev = new EventEmitter<WizardEvent>();

    @Output()
    public beforeChange = new EventEmitter<WizardEvent>();

    @Output()
    public afterNext = new EventEmitter<WizardEvent>();

    @Output()
    public afterPrev = new EventEmitter<WizardEvent>();

    @Output()
    public changeStep = new EventEmitter<WizardEvent>();

    @Output()
    public completeWizard = new EventEmitter<WizardEvent>();

    @ContentChildren(WizardStepComponent, { descendants: true })
    public wizardSteps: QueryList<WizardStepComponent> | undefined;

    @ViewChild('wizard')
    public wizardRef: ElementRef | undefined;

    public $steps: Observable<WizardStepComponent[]> | undefined;

    private stopped = false;
    private _currentStepIndex = 1;
    private _isCompleted = false;

    constructor(private _cd: ChangeDetectorRef) {}

    public ngOnInit(): void {
        this.$steps = of(['']).pipe(
            delay(0),
            map(() => {
                return this.steps;
            })
        );
    }

    public ngAfterViewInit(): void {
        this.init();
        setTimeout(() => {
            this.wizardSteps?.forEach((step, index) => {
                step.index = index;
                step.wizard = this;
            });
            if (this.steps && this.steps.length && !this.activeStep) {
                this.steps[0].isActive = true;
                this._cd.markForCheck();
            }
        });
    }

    /**
     * Get current step number
     */
    public get currentStepIndex(): number {
        return this._currentStepIndex;
    }

    public get steps(): WizardStepComponent[] {
        if (this.wizardSteps) {
            return this.wizardSteps.toArray();
        }
        return [];
    }

    get isCompleted(): boolean {
        return this._isCompleted;
    }

    public get totalSteps(): number {
        return this.steps.length;
    }

    public get activeStep(): WizardStepComponent | undefined {
        return this.steps.find((step) => step.isActive);
    }

    public set activeStep(step: WizardStepComponent | undefined) {
        if (!step) return;
        if (step !== this.activeStep) {
            const event = {
                target: this,
            } as WizardEvent;
            this.beforeChange.emit(event);
            if (this.activeStep) {
                this.activeStep.isActive = false;
            }
            step.isActive = true;
            this._cd.markForCheck();

            // == Trigger change event
            this.changeStep.emit(event);
        }
    }

    public next(): void {
        const step = this.activeStep;
        if (!step) return;
        if (!step.isValid) {
            return;
        }
        const val = step.beforeNext();
        if (val instanceof Promise) {
            val.then((ok) => {
                if (ok) {
                    this.goTo(this.getNextStep());
                }
            });
        } else {
            if (val) {
                this.goTo(this.getNextStep());
            }
        }
    }

    /**
     * Go to the prev step
     */
    public previous(): void {
        this.goTo(this.getPrevStep());
        this.activeStep?.beforePrevious.emit();
    }

    /**
     * Go to the last step
     */
    public last(): void {
        this.goTo(this.totalSteps);
    }

    /**
     * Go to the first step
     */
    public first(): void {
        this.goTo(1);
    }

    public submit(): void {
        const step = this.activeStep;
        if (step) {
            if (this.validateAtTheEnd && !step.isValid) {
                return;
            }
            const fnComplete = () => {
                this._isCompleted = true;
                this.completeWizard.emit({
                    target: this,
                });
            };
            const val = step.beforeComplete();
            if (val instanceof Promise) {
                val.then((ok) => {
                    if (ok) {
                        fnComplete();
                    }
                });
            } else {
                fnComplete();
            }
        }
    }

    public getWizardNavigatorState(index: number): string {
        const i = index + 1;
        if (i === this.currentStepIndex) {
            return 'current';
        }
        if (i < this.currentStepIndex) {
            return 'done';
        }
        return 'pending';
    }

    public get state(): string {
        if (this.isLastStep()) {
            return 'last';
        } else if (this.isFirstStep()) {
            return 'first';
        } else if (this.isBetweenStep()) {
            return 'between';
        }
        return '';
    }

    public useSVG(step: WizardStepComponent) {
        return step.icon && step.icon.endsWith('.svg');
    }

    public handleNavSelect($event: Event, index: number) {
        if (index) {
            if (!this.manualStepForward) {
                if (
                    (index < this.currentStepIndex && this.activeStep?.forwardAccepted) ||
                    index === this.currentStepIndex + 1
                ) {
                    if (index === this.currentStepIndex + 1) {
                        this.next();
                    } else {
                        this.goTo(index);
                    }
                }
            } else {
                this.goTo(index);
            }
        }
    }

    /**
     * Init wizard
     */
    private init() {
        // == Variables
        this._currentStepIndex = 1;
        this.stopped = false;

        this.goTo(this.startStep);
    }

    /**
     * Get next step
     */
    private getNextStep(): number {
        if (this.totalSteps >= this.currentStepIndex + 1) {
            return this.currentStepIndex + 1;
        } else {
            return this.totalSteps;
        }
    }

    /**
     * Get prev step
     */
    private getPrevStep(): number {
        if (this.currentStepIndex - 1 >= 1) {
            return this.currentStepIndex - 1;
        } else {
            return 1;
        }
    }

    /**
     * Handles wizard click wizard
     */
    private goTo(index: number) {
        // == Skip if this step is already shown
        if (index === this.currentStepIndex || index > this.totalSteps || index < 0) {
            return;
        }

        // == Validate step number
        if (!index) {
            index = this.getNextStep();
        }

        const event = {
            target: this,
        } as WizardEvent;

        if (index > this.currentStepIndex) {
            this.beforeNext.emit(event);
        } else {
            this.beforePrev.emit(event);
        }

        // == Skip if stopped
        if (this.stopped === true) {
            this.stopped = false;
            return;
        }

        // == Set current step
        this._currentStepIndex = index;
        this.activeStep = this.steps[this._currentStepIndex - 1];

        // == After next and prev events
        if (index > this.startStep) {
            this.afterNext.emit(event);
        } else {
            this.afterPrev.emit(event);
        }

        return this;
    }

    private isFirstStep(): boolean {
        return this.currentStepIndex === 1;
    }

    private isLastStep(): boolean {
        return this.currentStepIndex === this.totalSteps;
    }

    private isBetweenStep(): boolean {
        return !this.isLastStep() && !this.isFirstStep();
    }
}
