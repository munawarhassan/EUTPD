import { Component, Input } from '@angular/core';

@Component({
    selector: 'app-mixed-widget5',
    templateUrl: './mixed-widget5.component.html',
})
export class MixedWidget5Component {
    @Input() color = '';
    @Input() image = '';
    @Input() title = '';
    @Input() time = '';
    @Input() description = '';
}
