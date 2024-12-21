import { Component, Input } from '@angular/core';

@Component({
    selector: 'app-base-tables-widget2',
    templateUrl: './base-tables-widget2.component.html',
})
export class BaseTablesWidget2Component {
    TABS: string[] = ['Month', 'Week', 'Day'];
    currentTab: string;
    @Input() cssClass = '';

    constructor() {
        this.currentTab = this.TABS[2];
    }

    setCurrentTab(tab: string) {
        this.currentTab = tab;
    }
}
