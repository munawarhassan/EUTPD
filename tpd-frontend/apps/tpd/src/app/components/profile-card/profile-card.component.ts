import {
    ChangeDetectorRef,
    Component,
    ElementRef,
    HostBinding,
    Input,
    OnChanges,
    Renderer2,
    SimpleChanges,
    ViewEncapsulation,
} from '@angular/core';
import { User, UserAdminService } from '@devacfr/core';
import { ImageHelper } from '@devacfr/util';
import { environment } from '@tpd/environments/environment';

import { map } from 'rxjs/operators';
import { SvgIcons } from '@devacfr/bootstrap';

@Component({
    selector: 'app-profile-card',
    templateUrl: './profile-card.component.html',
    styleUrls: ['./profile-card.component.scss'],
    encapsulation: ViewEncapsulation.None,
})
export class ProfileCardComponent implements OnChanges {
    @Input()
    public user: User | undefined;

    @HostBinding('class')
    public class = 'card-user-profile';

    public highestPermission: string | undefined;

    public readonly: boolean | undefined;

    public chatEnable = true;

    constructor(
        public svgIcons: SvgIcons,
        private _elementRef: ElementRef,
        private _renderer: Renderer2,
        private _userAdminService: UserAdminService,
        private _cd: ChangeDetectorRef
    ) {
        this.chatEnable = environment.features.chat.enable;
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.user && changes.user.currentValue) {
            this.init();
        }
    }

    private init(): void {
        if (!this.user) {
            return;
        }
        if (this.user.username) {
            this.readonly = !this.user.updatable;
            this._userAdminService
                .getHighestGlobalPermission(this.user.username)
                .pipe(map((permission) => permission.name))
                .subscribe((perm) => {
                    this.highestPermission = perm;
                    this._cd.markForCheck();
                });
        }
        const el = this._elementRef.nativeElement as Element;
        const avatarImage = el.querySelector('#m-profile-avatar-image') as HTMLImageElement;
        const avatarBadge = el.querySelector('#m-profile-avatar-badge') as HTMLElement;
        if (this.user.avatarUrl && avatarImage && avatarBadge) {
            ImageHelper.loadImage(this._renderer, this.user.avatarUrl, avatarImage, avatarBadge);
        }
    }
}
