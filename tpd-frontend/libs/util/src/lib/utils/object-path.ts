const toStr = Object.prototype.toString;

export type Path = (number | string)[] | number | string;

/**
 * implementation of object-path
 * fix issue https://github.com/mariocasciaro/object-path/issues/97
 */
export class ObjectPath {
    public set(obj: any, path: Path, value: any, doNotReplace?: boolean): any {
        if (typeof path === 'number') {
            path = [path];
        }
        if (!path || path.length === 0) {
            return obj;
        }
        if (typeof path === 'string') {
            return this.set(obj, path.split('.').map(this.getKey), value, doNotReplace);
        }
        const currentPath = path[0];
        const currentValue = this.getShallowProperty(obj, currentPath);
        if (path.length === 1) {
            // eslint-disable-next-line eqeqeq
            if (currentValue == undefined || !doNotReplace) {
                obj[currentPath] = value;
            }
            return currentValue;
        }

        // eslint-disable-next-line eqeqeq
        if (currentValue == undefined) {
            // check if we assume an array
            if (typeof path[1] === 'number') {
                obj[currentPath] = [];
            } else {
                obj[currentPath] = {};
            }
        }

        return this.set(obj[currentPath], path.slice(1), value, doNotReplace);
    }

    public insert(obj: any, path: Path, value: any, at: number) {
        let arr = this.get(obj, path);
        at = ~~at;
        if (!this.isArray(arr)) {
            arr = [];
            this.set(obj, path, arr);
        }
        (arr as Array<any>).splice(at, 0, value);
    }

    public empty(obj: any, path: Path) {
        if (this.isEmpty(path)) {
            return void 0;
        }
        if (obj == null) {
            return void 0;
        }

        const value = this.get(obj, path);
        let i;
        if (!value) {
            return void 0;
        }

        if (typeof value === 'string') {
            return this.set(obj, path, '');
        } else if (this.isBoolean(value)) {
            return this.set(obj, path, false);
        } else if (typeof value === 'number') {
            return this.set(obj, path, 0);
        } else if (this.isArray(value)) {
            value.length = 0;
        } else if (this.isObject(value)) {
            for (i in value) {
                if (this.hasShallowProperty(value, i)) {
                    delete value[i];
                }
            }
        } else {
            return this.set(obj, path, null);
        }
    }

    public ensureExists(obj: any, path: Path, value: any) {
        return this.set(obj, path, value, true);
    }

    public has(obj: any, paths: Path): boolean {
        if (typeof paths === 'number') {
            paths = [paths];
        } else if (typeof paths === 'string') {
            paths = paths.split('.');
        }

        if (!paths || paths.length === 0) {
            return !!obj;
        }

        for (const p of paths) {
            const j = this.getKey(p);

            if ((typeof j === 'number' && this.isArray(obj) && j < obj.length) || this._hasOwnProperty(obj, j)) {
                obj = obj[j];
            } else {
                return false;
            }
        }
        return true;
    }

    public push(obj: any, path: Path, value: any): any[] {
        let arr = this.get(obj, path);
        if (!this.isArray(arr)) {
            arr = [];
            this.set(obj, path, arr);
        }

        arr.push(value);
        return arr;
    }

    public remove(obj: any, path: Path, item: any) {
        const arr = this.get(obj, path) as any[];
        if (this.isArray(arr)) {
            const index = arr.indexOf(item);
            if (arr && index >= 0) {
                arr.splice(index, 1);
            }
            this.set(obj, path, arr);
        }
    }

    public coalesce(obj: any, paths: Path, defaultValue: any) {
        let value;
        if (typeof paths === 'number') {
            paths = [paths];
        } else if (typeof paths === 'string') {
            paths = paths.split('.');
        }
        for (let i = 0, len = paths.length; i < len; i++) {
            value = this.get(obj, paths[i]);
            if (value != null) {
                return value;
            }
        }

        return defaultValue;
    }

    public get(obj: any, path: Path, defaultValue?: any): any {
        if (typeof path === 'number') {
            path = [path];
        }
        if (!path || path.length === 0) {
            return obj;
        }
        if (obj == null) {
            return defaultValue;
        }
        if (typeof path === 'string') {
            return this.get(obj, path.split('.'), defaultValue);
        }

        const currentPath = this.getKey(path[0]);
        const nextObj = this.getShallowProperty(obj, currentPath);
        // eslint-disable-next-line eqeqeq
        if (nextObj == undefined) {
            return defaultValue;
        }

        if (path.length === 1) {
            return nextObj;
        }

        return this.get(obj[currentPath], path.slice(1), defaultValue);
    }

    public del(obj: any, path: Path): any {
        if (typeof path === 'number') {
            path = [path];
        }

        if (obj == null) {
            return obj;
        }

        if (this.isEmpty(path)) {
            return obj;
        }
        if (typeof path === 'string') {
            return this.del(obj, path.split('.'));
        }

        const currentPath = this.getKey(path[0]);
        if (!this.hasShallowProperty(obj, currentPath)) {
            return obj;
        }

        if (path.length === 1) {
            if (this.isArray(obj)) {
                obj.splice(currentPath, 1);
            } else {
                delete obj[currentPath];
            }
        } else {
            return this.del(obj[currentPath], path.slice(1));
        }

        return obj;
    }

    public toString(type: any) {
        return toStr.call(type);
    }

    private hasShallowProperty(obj: any, prop: any) {
        return (typeof prop === 'number' && Array.isArray(obj)) || this._hasOwnProperty(obj, prop);
    }

    private getShallowProperty(obj: any, prop: any): any {
        if (this.hasShallowProperty(obj, prop)) {
            const val = obj[prop];
            if (val === null) return undefined;
            return val;
        }
    }

    private _hasOwnProperty(obj: any, prop: any): boolean {
        if (obj == null) {
            return false;
        }
        if (prop in obj) return true;
        if (typeof obj === 'object') {
            return Object.prototype.hasOwnProperty.call(obj, prop);
        }
        return false;
    }

    private isObject(obj: unknown): boolean {
        return typeof obj === 'object' && this.toString(obj) === '[object Object]';
    }

    private isArray(obj: unknown): boolean {
        return obj instanceof Array;
    }
    private isBoolean(obj: unknown): boolean {
        return typeof obj === 'boolean' || this.toString(obj) === '[object Boolean]';
    }

    private getKey(key: string | number): string | number {
        if (typeof key == 'number') return key;
        const intKey = parseInt(key);
        if (intKey.toString() === key) {
            return intKey;
        }
        return key;
    }

    private isEmpty(value: any): boolean {
        if (!value) {
            return true;
        }
        if (this.isArray(value) && value.length === 0) {
            return true;
        } else if (typeof value !== 'string') {
            for (const i in value) {
                if (this._hasOwnProperty(value, i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}

export const objectPath = new ObjectPath();
