import { Observable, Observer } from 'rxjs';

export function fromFunction(factory: () => unknown): Observable<unknown> {
    return new Observable((observer: Observer<unknown>) => {
        try {
            observer.next(factory());
            observer.complete();
        } catch (error) {
            observer.error(error);
        }
    });
}

export function sleep(milliseconds: number) {
    const date = Date.now();
    let currentDate;
    do {
        currentDate = Date.now();
    } while (currentDate - date < milliseconds);
}
