import { Component, Input } from '@angular/core';

@Component({
    selector: 'app-alert-keystore',
    template: `
        <div class="alert alert-outline-danger fade show" role="alert">
            <div class="alert-icon"><i class="flaticon-warning-sign"></i></div>
            <div class="alert-text" [innerHTML]="message"></div>
        </div>
    `,
})
export class AlertKeystoreComponent {
    @Input()
    public message: string | undefined;
}
