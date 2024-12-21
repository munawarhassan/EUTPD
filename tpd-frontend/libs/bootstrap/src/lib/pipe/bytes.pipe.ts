import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'bytes',
})
export class BytesPipe implements PipeTransform {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    transform(value: any, ...args: any[]): any {
        return this.toStringDecimal(value);
    }

    private toStringDecimal(bytes: number) {
        const unit = 1000;
        if (bytes < unit) {
            return bytes + ' B';
        }
        const exp = Math.trunc(Math.log(bytes) / Math.log(unit));
        const pre = 'kMGTPE'.charAt(exp - 1);
        const val = bytes / Math.pow(unit, exp);
        return val.toFixed(1) + ' ' + pre + 'B';
    }
}
