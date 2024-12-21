import { Directive, ElementRef, HostBinding, Input, OnDestroy } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { UserService } from '@devacfr/core';
import { ReplaySubject, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';

// eslint-disable-next-line @angular-eslint/directive-selector
@Directive({ selector: 'img[avatarUrl]' })
export class AvatarUrlDirective implements OnDestroy {
    @HostBinding('alt')
    public alt = 'avatar';

    @Input()
    public set avatarUrl(value: string) {
        if (value != null) {
            this.avatarChange.next(value);
        }
    }

    @Input()
    public size = 32;

    private avatarChange = new ReplaySubject<string>();
    private _subscription = new Subscription();

    constructor(
        private _svgIcons: SvgIcons,
        private _userService: UserService,
        private _element: ElementRef<HTMLImageElement>
    ) {
        this._element.nativeElement.src = this._svgIcons.Avatar.blank;
        this._subscription.add(
            this.avatarChange
                .pipe(switchMap((avatar) => this._userService.getAvatarUrl(avatar, this.size)))
                .subscribe((url) => (this._element.nativeElement.src = url.href))
        );
    }

    ngOnDestroy(): void {
        this._subscription.unsubscribe();
    }
}
