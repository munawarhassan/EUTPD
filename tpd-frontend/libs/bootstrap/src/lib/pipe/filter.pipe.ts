import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'filter',
})
export class FilterPipe implements PipeTransform {
    public transform<T>(items: T[], field: string, value: string, mode?: string): T[] {
        if (!items) {
            return [];
        }
        if (!value || value.length === 0) {
            return items;
        }
        return items.filter((it) => {
            if (mode === 'strict') {
                return it[field] === value;
            } else {
                return it[field].toLowerCase().indexOf(value.toLowerCase()) !== -1;
            }
        });
    }
}
