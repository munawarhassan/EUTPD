import { Component, Input } from '@angular/core';

@Component({
    selector: 'app-layout-view',
    templateUrl: 'layout-view.component.html',
})
export class LayoutViewComponent {
    @Input()
    public width: 'fluid' | 'fixed' = 'fixed';

    public currentDateStr: string = new Date().getFullYear().toString();

    public onAsideActivated(event) {
        //
    }

    public onAsideDesactivated(event) {
        //
    }
}
