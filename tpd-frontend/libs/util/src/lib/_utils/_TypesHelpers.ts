export function getObjectPropertyValueByKey(
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    obj: any,
    key: string
): unknown | undefined {
    const map = new Map(Object.entries(obj));
    if (Object.prototype.hasOwnProperty.call(obj, key) && map) {
        return map.get(key);
    }
    return undefined;
}

/**
 * Generates unique ID for give prefix.
 * @param {string} prefix Prefix for generated ID
 * @returns {boolean}
 */
export function getUniqueIdWithPrefix(prefix: string | undefined): string {
    const result = Math.floor(Math.random() * new Date().getTime()).toString();
    if (!prefix) {
        return result;
    }

    return `${prefix}${result}`;
}

/* eslint-disable no-useless-escape */
export function stringSnakeToCamel(str: string): string {
    return str.replace(/(\-\w)/g, function (m) {
        return m[1].toUpperCase();
    });
}

export function toJSON(value: string | JSON): JSON | undefined {
    if (typeof value !== 'string') {
        return value;
    }

    if (!value) {
        return undefined;
    }

    // ("'" => "\"");
    const result = value
        .toString()
        .split('')
        .map((el) => (el !== "'" ? el : '"'))
        .join('');
    const jsonStr = result.replace(/(\w+:)|(\w+ :)/g, (matched) => {
        return '"' + matched.substring(0, matched.length - 1) + '":';
    });
    try {
        return JSON.parse(jsonStr);
    } catch {
        return undefined;
    }
}
