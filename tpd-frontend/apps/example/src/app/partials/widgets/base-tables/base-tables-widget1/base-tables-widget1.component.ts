import { Component, Input, OnInit } from '@angular/core';

@Component({
    selector: 'app-base-tables-widget1',
    templateUrl: './base-tables-widget1.component.html',
})
export class BaseTablesWidget1Component implements OnInit {
    TABS: string[] = ['Month', 'Week', 'Day'];
    currentTab: string;
    @Input() cssClass = '';
    @Input() progressWidth = '';

    constructor() {
        this.currentTab = this.TABS[2];
    }

    ngOnInit(): void {
        if (!this.progressWidth) {
            this.progressWidth = 'min-w-200px';
        }
    }

    setCurrentTab(tab: string) {
        this.currentTab = tab;
    }
}
