import { Pipe, PipeTransform } from '@angular/core';
import { isArray } from 'lodash-es';

/**
 * Returns string from Array
 */
@Pipe({
    name: 'split',
})
export class SplitPipe implements PipeTransform {
    /**
     * Transform
     *
     * @param value: any
     * @param args: any
     */
    transform(value: unknown, separator = ','): unknown[] {
        if (typeof value === 'string') {
            return value.split(separator);
        }
        if (isArray(value)) {
            return value;
        }
        return [value];
    }
}
