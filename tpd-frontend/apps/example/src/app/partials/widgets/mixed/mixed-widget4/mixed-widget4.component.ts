import { Component, Input } from '@angular/core';

@Component({
    selector: 'app-mixed-widget4',
    templateUrl: './mixed-widget4.component.html',
})
export class MixedWidget4Component {
    @Input() color = '';
    @Input() image = '';
    @Input() title = '';
    @Input() date = '';
    @Input() progress = '';
}
