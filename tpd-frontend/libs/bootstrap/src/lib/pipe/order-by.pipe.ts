import { Injectable, Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'orderBy' })
@Injectable()
export class OrderByPipe implements PipeTransform {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    public transform(records: any[], property: string, descending: boolean): any {
        if (!records) {
            return null;
        }
        const direction = descending ? 1 : -1;
        return records.sort(function (a, b) {
            if (a[property] < b[property]) {
                return -1 * direction;
            } else if (a[property] > b[property]) {
                return 1 * direction;
            } else {
                return 0;
            }
        });
    }
}
