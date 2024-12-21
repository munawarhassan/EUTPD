import { Breakpoint, BreakpointValue } from './typing';
import { isArray, isString } from 'lodash-es';

export class ClassBuilder {
    private _styles: string[] = [];
    private namespace?: string;

    public static create(...clazz: string[]): ClassBuilder {
        return new ClassBuilder(...clazz);
    }

    constructor(...clazz: string[]) {
        this.css(...clazz);
    }

    public ns(namespace: string): ClassBuilder {
        this.namespace = namespace;
        return this;
    }

    public css(...clazz: string[]): ClassBuilder {
        if (clazz.length === 1) {
            if (clazz[0]) {
                clazz = clazz[0].split(' ');
            } else {
                return this;
            }
        }
        this._styles = this._styles.concat(clazz);
        return this;
    }

    public flag(prefix: string, ...flags: string[]): ClassBuilder {
        if (!flags || !prefix) {
            return this;
        }
        let attr = flags;
        if (isString(attr)) {
            attr = attr.split(' ');
        }
        if (isArray(attr)) {
            if (attr.length === 1) {
                if (attr[0]) {
                    attr = attr[0].split(' ');
                } else {
                    return this;
                }
            }
            attr.forEach((flag) => this.css(`${prefix}${flag}`));
        }

        return this;
    }

    public breakpoint(prefix: string, breakpoint: BreakpointValue<string>): ClassBuilder {
        if (!breakpoint || !prefix) {
            return this;
        }
        let ar: string[] = [];
        if (isString(breakpoint)) {
            ar = breakpoint.split(' ');
        } else {
            for (const key in breakpoint) {
                if (Object.prototype.hasOwnProperty.call(breakpoint, key)) {
                    const device = key as Breakpoint;
                    const value = breakpoint[device];
                    if (!value) continue;
                    if (device === 'default') {
                        ar.push(value);
                    } else {
                        ar.push(`${breakpoint}-${value}`);
                    }
                }
            }
        }
        ar.forEach((cl) => this.css(`${prefix}${cl}`));

        return this;
    }

    public forEach(callbackfn: (value: string, index: number, array: string[]) => void): void {
        this._styles.forEach(callbackfn);
    }

    public toString(): string {
        if (this.namespace) {
            this._styles = this._styles.map((style) => (style ? `${this.namespace}${style}` : style));
        }
        return this._styles.join(' ');
    }
}
