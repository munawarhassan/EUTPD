import { ChangeDetectorRef, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { BsColor } from '@devacfr/util';
import { Subscription } from 'rxjs';
import { AuthNotice, AuthNoticeService } from '../auth-notice.service';

@Component({
    selector: 'app-auth-notice',
    templateUrl: './auth-notice.component.html',
})
export class AuthNoticeComponent implements OnInit, OnDestroy {
    @Input()
    public color: BsColor = 'danger';

    @Input()
    public message = '';

    // Private properties
    private subscriptions: Subscription[] = [];

    /**
     * Component Constructure
     *
     * @param authNoticeService
     * @param cdr
     */
    constructor(public authNoticeService: AuthNoticeService, private cdr: ChangeDetectorRef) {}

    /**
     * On init
     */
    ngOnInit() {
        this.subscriptions.push(
            this.authNoticeService.onNoticeChanged$.subscribe((notice: AuthNotice) => {
                notice = Object.assign({}, { message: '', type: '' }, notice);
                this.message = notice.message;
                this.color = notice.type;
                this.cdr.markForCheck();
            })
        );
    }

    /**
     * On destroy
     */
    ngOnDestroy(): void {
        this.subscriptions.forEach((sb) => sb.unsubscribe());
    }
}
