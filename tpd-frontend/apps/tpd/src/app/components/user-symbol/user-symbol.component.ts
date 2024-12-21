import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    HostBinding,
    Input,
    OnChanges,
    OnInit,
    SimpleChanges,
} from '@angular/core';
import { User } from '@devacfr/core';
import { ClassBuilder } from '@devacfr/util';
import _ from 'lodash-es';
import { EMPTY, Observable, ReplaySubject } from 'rxjs';
import { map, tap } from 'rxjs/operators';

@Component({
    selector: 'app-user-symbol',
    templateUrl: './user-symbol.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserSymbolComponent implements OnInit, OnChanges {
    @HostBinding('class')
    public get class() {
        const builder = ClassBuilder.create(this._class);
        if (this.hover) builder.css('symbol-hover');
        return builder.toString();
    }

    @Input()
    public users: User[] | User | ((obs: Observable<unknown>) => Observable<User[]>) | undefined;

    @Input()
    public size = 6;

    @Input()
    public hover = false;

    @Input()
    public total = 0;

    public users$: Observable<User[]> | undefined;
    private _usersSubject = new ReplaySubject<unknown>();

    private _class = 'symbol-group';
    constructor(private _cd: ChangeDetectorRef) {}

    ngOnInit(): void {
        this.users$ = this.createObserver();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.users && changes.users.currentValue !== changes.users.previousValue) {
            if (changes.users.firstChange) {
                this._usersSubject.next(changes.users.firstChange);
            } else if (typeof changes.users.currentValue !== 'function') {
                this._usersSubject.next(changes.users.currentValue);
            }
        }
    }

    public get nextTotal(): number {
        return this.total - this.size;
    }

    private createObserver(): Observable<User[]> {
        return this._usersSubject.pipe(
            (obs: Observable<unknown>) => {
                if (!this.users) return EMPTY;
                if (typeof this.users !== 'function') {
                    return obs.pipe(map(() => (_.isArray(this.users) ? (this.users as User[]) : [this.users as User])));
                } else {
                    return this.users(obs);
                }
            },
            tap((users) => {
                this.total = users.length;
                this._cd.markForCheck();
            }),
            map((users) => users.slice(0, this.size))
        );
    }
}
