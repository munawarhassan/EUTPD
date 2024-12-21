import { Observable } from 'rxjs';

export interface WebSocketProvider<T> {
    watch(topic: string): Observable<T>;

    publish(broker: string, message: unknown, priority?: number): void;

    connect(): void;
}
