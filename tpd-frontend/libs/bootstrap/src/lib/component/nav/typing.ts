import { QueryList } from '@angular/core';

export interface TabPanel {
    active?: boolean;
}

export interface RootTab {
    linkClass: string;
    tabset: QueryList<TabPanel> | undefined;
}
