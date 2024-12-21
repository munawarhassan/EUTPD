import { Component, Input } from '@angular/core';

@Component({
    selector: 'app-alert-profile',
    template: `
        <div class="alert alert-outline-brand fade show" role="alert">
            <div class="alert-icon"><i class="flaticon-questions-circular-button"></i></div>
            <div class="alert-text" [innerHTML]="message"></div>
        </div>
    `,
})
export class AlertProfileComponent {
    @Input()
    public message: string | undefined;
}
