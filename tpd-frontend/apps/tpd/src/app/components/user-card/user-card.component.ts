import { ChangeDetectionStrategy, Component, HostBinding, Input } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { User, UserAdminService } from '@devacfr/core';
import { ClassBuilder } from '@devacfr/util';
import { environment } from '@tpd/environments/environment';
import { BehaviorSubject, EMPTY, Observable } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';

@Component({
    selector: 'app-user-card',
    templateUrl: './user-card.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserCardComponent {
    @Input()
    public layer: 'line' | 'card' = 'line';

    @Input()
    public link: unknown[] | string | undefined;

    @Input()
    public set user(value: User | undefined) {
        this._userSubject.next(value);
    }

    public get user(): User | undefined {
        return this._userSubject.value;
    }

    @HostBinding('class')
    @Input('class')
    public get cssClass(): string {
        const builder = ClassBuilder.create();
        if (this.layer === 'line') builder.css('d-flex flex-row position-relative');
        if (this._cssClass) builder.css(this._cssClass);
        return builder.toString();
    }

    public set cssClass(value: string) {
        this._cssClass = value;
    }

    public chatEnable = true;
    private _userSubject = new BehaviorSubject<User | undefined>(undefined);
    public highestPermission$: Observable<string>;

    private _cssClass: string | undefined;

    constructor(public svgIcons: SvgIcons, private _userAdminService: UserAdminService) {
        this.chatEnable = environment.features.chat.enable;
        this.highestPermission$ = this._userSubject.pipe(
            switchMap((user) => {
                if (!user) return EMPTY;
                return this._userAdminService
                    .getHighestGlobalPermission(user.username)
                    .pipe(map((permission) => permission.name));
            })
        );
    }
}
