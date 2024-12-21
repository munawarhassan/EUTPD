import { Component } from '@angular/core';
import { EventManager } from '@devacfr/layout';

@Component({
    selector: 'app-footer',
    templateUrl: 'footer.component.html',
})
export class FooterComponent {
    public currentDateStr: string = new Date().getFullYear().toString();

    constructor(private _eventManager: EventManager) {}

    public team() {
        this._eventManager.broadcast({ target: this, name: 'team' });
    }

    public about() {
        this._eventManager.broadcast({ target: this, name: 'about' });
    }
}
