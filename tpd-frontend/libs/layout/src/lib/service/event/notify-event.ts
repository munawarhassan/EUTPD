import { Event } from './event';

export class NotifyEvent extends Event {
    public static create(target: unknown, message: string, notify = true): NotifyEvent {
        return new NotifyEvent(target, message, notify);
    }

    /**
     * @param  {any} readonly target
     * @param  {string} readonly message?
     * @param  {boolean=true} readonlynotify
     */
    constructor(
        public readonly target: unknown,
        public readonly message?: string,
        public readonly notify: boolean = true
    ) {
        super(target, 'notify-event');
    }
}
