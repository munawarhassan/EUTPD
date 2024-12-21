// Angular
import { Pipe, PipeTransform } from '@angular/core';

/**
 * Returns only first letter of string
 */
@Pipe({
    name: 'firstLetter',
})
export class FirstLetterPipe implements PipeTransform {
    /**
     * Transform
     *
     * @param value: any
     * @param args: any
     */
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    transform(value: string | undefined, args?: unknown): string {
        if (value) {
            value = value.replace(/,/g, '');
            return value
                .split(' ')
                .map((n) => n[0])
                .join('');
        }
        return '';
    }
}
