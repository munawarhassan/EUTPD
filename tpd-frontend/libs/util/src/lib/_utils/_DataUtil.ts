export class DataUtil {
    static store: Map<Element, Map<string, unknown>> = new Map();

    public static set(instance: Element | undefined, key: string, data: unknown): void {
        if (!instance) {
            return;
        }

        const instanceData = DataUtil.store.get(instance);
        if (!instanceData) {
            const newMap = new Map().set(key, data);
            DataUtil.store.set(instance, newMap);
            return;
        }

        instanceData.set(key, data);
    }

    public static get(instance: Element, key: string): unknown | undefined {
        const instanceData = DataUtil.store.get(instance);
        if (!instanceData) {
            return;
        }

        return instanceData.get(key);
    }

    public static remove(instance: Element, key: string): void {
        const instanceData = DataUtil.store.get(instance);
        if (!instanceData) {
            return;
        }

        instanceData.delete(key);
    }

    public static has(instance: Element, key: string): boolean {
        const instanceData = DataUtil.store.get(instance);
        if (instanceData) {
            return instanceData.has(key);
        }

        return false;
    }

    public static getAllInstancesByKey(key: string) {
        const result: unknown[] = [];
        DataUtil.store.forEach((val) => {
            val.forEach((v, k) => {
                if (k === key) {
                    result.push(v);
                }
            });
        });
        return result;
    }
}
