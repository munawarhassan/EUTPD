import { Component } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Select2Event, Select2Observer, Select2PrefetchEvent, Select2SearchEvent } from '@devacfr/forms';
import { EMPTY, from, Observable } from 'rxjs';
import { filter, switchMap, toArray } from 'rxjs/operators';
import { UsersData } from '../data';

function createObserver<O>(
    source: O[],
    predicate: {
        is: (event: Select2PrefetchEvent, value: O) => boolean;
        match: (event: Select2SearchEvent, value: O) => boolean;
    }
): Select2Observer<O> {
    let obs = from(source);
    return (event$: Observable<Select2Event>) =>
        event$.pipe(
            switchMap((event: Select2Event) => {
                if (event.state === 'prefetch') {
                    return obs.pipe(
                        filter((v) => predicate.is(event, v)),
                        toArray()
                    );
                } else if (event.state === 'search') {
                    if (event.termMatch || event.termMatch === '') {
                        obs = obs.pipe(filter((v) => predicate.match(event, v)));
                    }
                    return obs.pipe(toArray());
                }
                return EMPTY;
            })
        );
}

@Component({
    selector: 'app-select2-example',
    templateUrl: './select2-example.component.html',
})
export class Select2ExampleComponent {
    public state1!: string;
    public state2!: string;
    public state3!: string;
    public state4!: string;

    public states: any;
    public tags: any;

    public stateUser = UsersData[3];

    public stateUserId = UsersData[3]._id;

    public users = UsersData;

    public users$ = createObserver(UsersData, {
        is: (event, value) => {
            const id = value._id as string;
            return event.selected.includes(id);
        },
        match: (event, value) => {
            const name = value.name as string;
            return name.toLowerCase().indexOf(event.termMatch as string) > 0;
        },
    });

    public submit(form: NgForm): void {
        if (form.invalid) return;
    }
}
