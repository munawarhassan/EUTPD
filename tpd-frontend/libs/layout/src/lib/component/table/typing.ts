export interface TableColumn {
    name: string;
    title?: string;
    i18n?: string;
    sort:
        | {
              ignoreCase: boolean;
          }
        | boolean
        | undefined;
    class?: string;
    align?: 'start' | 'end' | 'center';
}

export interface TableOptions {
    checkedRow?: boolean;
    /**
     * Enable or disable table pagination.
     */
    pagination?: 'fixed' | 'infinite' | 'none';
    scrollTrack?: 'all' | 'horizontal' | 'vertical';
    infiniteScrollDistance?: number;
    infiniteScrollThrottle?: number;
    minThumbSize?: number;

    columns: TableColumn[];
}

export type TableLayer = 'card' | 'line';

export type SelectedTableItem<T> = T & { selected?: boolean };
