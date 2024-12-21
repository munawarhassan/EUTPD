import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export type NoticeType = 'success' | 'danger' | 'info' | 'warning';
export interface AuthNotice {
    type: NoticeType;
    message: string;
}

@Injectable({
    providedIn: 'root',
})
export class AuthNoticeService {
    onNoticeChanged$: Subject<AuthNotice>;

    constructor() {
        this.onNoticeChanged$ = new Subject();
    }

    emit(message: string, type: NoticeType) {
        const notice: AuthNotice = {
            message,
            type,
        };
        this.onNoticeChanged$.next(notice);
    }
}
