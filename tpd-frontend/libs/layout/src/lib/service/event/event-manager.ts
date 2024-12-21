import { Injectable } from '@angular/core';
import { Observable, Observer, PartialObserver, Subscription } from 'rxjs';
import { filter, share } from 'rxjs/operators';
import { Event } from '.';

@Injectable({ providedIn: 'root' })
export class EventManager {
    private _observable: Observable<Event>;
    private _observer: Observer<Event> | undefined;

    constructor() {
        this._observable = new Observable<Event>((observer) => {
            this._observer = observer;
        }).pipe(share());
    }

    /**
     * Method to broadcast the event to observer
     */
    public broadcast(event: Event): void {
        if (this._observer != null) {
            this._observer.next(event);
        }
    }

    /**
     * Method to subscribe to an event with callback
     */
    public subscribe(eventName: string, callback: PartialObserver<Event>): Subscription {
        const subscriber = this._observable
            .pipe(
                filter((event) => {
                    return event.name === eventName;
                })
            )
            .subscribe(callback);
        return subscriber;
    }

    /**
     * Method to unsubscribe the subscription
     */
    public unsubscribe(subscriber: Subscription): void {
        subscriber.unsubscribe();
    }
}
