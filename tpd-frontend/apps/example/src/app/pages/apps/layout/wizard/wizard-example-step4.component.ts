import { Component, Input } from '@angular/core';

@Component({
    selector: 'app-wizard-example-step4',
    templateUrl: './wizard-example-step4.component.html',
})
export class WizardExampleStep4Component {
    @Input()
    public user: any | undefined;
}
