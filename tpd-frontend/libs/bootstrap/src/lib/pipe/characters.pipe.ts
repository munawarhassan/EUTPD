import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'characters' })
export class CharactersPipe implements PipeTransform {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    public transform(value: any, chars: number, breakOnWord?: boolean) {
        if (isNaN(chars)) {
            return value;
        }
        if (chars <= 0) {
            return '';
        }
        if (value && value.length > chars) {
            value = value.substring(0, chars);

            if (!breakOnWord) {
                const lastspace = value.lastIndexOf(' ');
                // Get last space
                if (lastspace !== -1) {
                    value = value.substr(0, lastspace);
                }
            } else {
                while (value.charAt(value.length - 1) === ' ') {
                    value = value.substr(0, value.length - 1);
                }
            }
            return value + '...';
        }
        return value;
    }
}
