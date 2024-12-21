import { Observable } from 'rxjs';
import { DataFormat, GroupedDataFormat } from 'select2';

export type Select2Value = string | Record<string, unknown> | DataFormat | GroupedDataFormat | unknown;

export type Select2Data = Select2Value | Select2Value[];

export type SelectTrigger2Event = {
    state: 'initial';
};

export type Select2SearchEvent = {
    termMatch?: string | undefined;
    state: 'search';
};

export type Select2PrefetchEvent = {
    selected: string[];
    state: 'prefetch';
};

export type Select2Event = SelectTrigger2Event | Select2PrefetchEvent | Select2SearchEvent;

export type Select2Observer<T = Select2Value> = (event: Observable<Select2Event>) => Observable<T | T[]>;
