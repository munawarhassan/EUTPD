import { Component, Input } from '@angular/core';

@Component({
    selector: 'lt-stepper',
    templateUrl: 'stepper.component.html',
    styleUrls: ['./stepper.component.scss'],
})
export class StepperComponent {
    @Input()
    public mode: 'vertical' | 'horizontal' = 'horizontal';

    @Input()
    public panelClass = '';
}
