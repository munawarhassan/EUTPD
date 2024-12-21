/**
 *
 */
export abstract class Event {
    public readonly name: string;

    /**
     * @param  {any} readonly target
     * @param  {string} readonly name
     */
    constructor(public readonly target: unknown, name: string) {
        this.name = name;
    }
}
