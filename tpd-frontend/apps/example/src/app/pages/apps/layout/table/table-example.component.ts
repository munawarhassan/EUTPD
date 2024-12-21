import { Component } from '@angular/core';
import { Page, Pageable } from '@devacfr/util';
import { TableOptions, PageChangedEvent } from '@devacfr/layout';
import { UsersData } from '../data';

@Component({
    selector: 'app-table-example',
    templateUrl: 'table-example.component.html',
})
export class TableExampleComponent {
    public page: Page<any>;

    public tableOptions: TableOptions = {
        columns: [
            { name: 'name', i18n: 'name', sort: true },
            { name: 'email', i18n: 'email', sort: true },
            { name: 'phone', i18n: 'phone', sort: true },
            { name: 'gender', i18n: 'gender', sort: true },
            { name: 'company', i18n: 'company', sort: true },
        ],
    };

    constructor() {
        this.page = Page.of({ content: UsersData, pageable: Pageable.of(0, 20) });
    }

    public refresh(pageable?: Pageable): void {
        if (!pageable) pageable = this.page.pageable;
        this.page = Page.of({ content: UsersData, pageable });
    }

    public handlePaginationChanged(ev: PageChangedEvent): void {
        this.refresh(ev.pageable);
    }
}
