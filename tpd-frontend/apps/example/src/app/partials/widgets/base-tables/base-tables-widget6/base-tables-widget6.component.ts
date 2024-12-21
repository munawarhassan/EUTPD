import { Component, Input } from '@angular/core';

@Component({
    selector: 'app-base-tables-widget6',
    templateUrl: './base-tables-widget6.component.html',
})
export class BaseTablesWidget6Component {
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
