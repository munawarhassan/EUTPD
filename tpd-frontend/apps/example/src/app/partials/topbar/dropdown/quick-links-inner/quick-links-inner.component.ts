import { Component, Input } from '@angular/core';

@Component({
    selector: 'app-quick-links-inner',
    templateUrl: './quick-links-inner.component.html',
})
export class QuickLinksInnerComponent {
    @Input()
    public iconSize = '';

    @Input()
    public buttonHeight = '';
}
