import { Pipe, PipeTransform } from '@angular/core';
import { objectPath } from '@devacfr/util';

/**
 * Returns object from parent object
 */
@Pipe({
    name: 'objectPath',
})
export class ObjectPathPipe implements PipeTransform {
    /**
     * Transform
     *
     * @param value: any
     * @param args: any
     */
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    transform(value: any, args?: any): any {
        return objectPath.get(value, args);
    }
}
