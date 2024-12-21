/**
 *
 * @param value true si et seulement si la propriété doit apparaître lors d'une énumération des propriétés de l'objet.
 * @returns
 */
export function Enumerable(value: boolean) {
    return function (target: unknown, propertyKey: string) {
        const descriptor = Object.getOwnPropertyDescriptor(target, propertyKey) || {};
        if (descriptor.enumerable !== value) {
            descriptor.enumerable = value;
            descriptor.writable = true;
            Object.defineProperty(target, propertyKey, descriptor);
        }
    };
}
