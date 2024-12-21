import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Page, Pageable } from '@devacfr/util';
import moment from 'moment';
import { Observable } from 'rxjs';
import { AuditEntity } from '.';
import { BACKEND_SERVER_API_URL_TOKEN } from '../shared';

export enum Channel {
    ADMIN_LOG = 'audit.channel.admin_log',
    AUTHENTICATION = 'audit.channel.authentication',
    APPLICATION_CONFIGURATION = 'audit.channel.application_configuration',
    SECURITY = 'audit.channel.security',
    PERMISSION = 'audit.channel.permission',
    KEYSTORE = 'audit.channel.keystore',
    EUCEG = 'audit.channel.euceg',
    PRODUCT = 'audit.channel.euceg.product',
    SUBMISSION = 'audit.channel.euceg.submission',
    ATTACHMENT = 'audit.channel.euceg.attachment',
}

export type ChannelType = {
    channel: Channel;
    name: string;
};

export const Channels: { [key in keyof typeof Channel]: ChannelType } = {
    ADMIN_LOG: { channel: Channel.ADMIN_LOG, name: 'Admin Log' },
    AUTHENTICATION: { channel: Channel.AUTHENTICATION, name: 'Authentication' },
    APPLICATION_CONFIGURATION: { channel: Channel.APPLICATION_CONFIGURATION, name: 'Settings Log' },
    SECURITY: { channel: Channel.SECURITY, name: 'Security Log' },
    PERMISSION: { channel: Channel.PERMISSION, name: 'Permission Log' },
    KEYSTORE: { channel: Channel.KEYSTORE, name: 'Keystore Log' },
    EUCEG: { channel: Channel.EUCEG, name: 'Euceg Log' },
    PRODUCT: { channel: Channel.PRODUCT, name: 'Product Log' },
    SUBMISSION: { channel: Channel.SUBMISSION, name: 'Submission Log' },
    ATTACHMENT: { channel: Channel.ATTACHMENT, name: 'Attachment Log' },
};

@Injectable({ providedIn: 'any' })
export class AuditsService {
    private FORMAT_DATE = 'YYYY-MM-DD';

    constructor(
        @Inject(BACKEND_SERVER_API_URL_TOKEN) private BACKEND_SERVER_API_URL: string,
        private httpClient: HttpClient
    ) {}

    public findAll(): Observable<AuditEntity[]> {
        return this.httpClient.get<AuditEntity[]>(this.BACKEND_SERVER_API_URL + 'audits/all');
    }

    public findByDates(
        pageable: Pageable,
        fromDate?: Date,
        toDate?: Date,
        ...channels: Channel[]
    ): Observable<Page<AuditEntity>> {
        let params = pageable.httpParams();
        if (fromDate) {
            params = params.append('fromDate', this.formatDate(fromDate));
        }
        if (toDate) {
            params = params.append('toDate', this.formatDate(toDate));
        }
        if (channels && channels.length > 0) {
            params = params.append('channels', channels.join(','));
        }
        return this.httpClient
            .get<Page<AuditEntity>>(this.BACKEND_SERVER_API_URL + 'audits/byDates', {
                params,
            })
            .pipe(Page.mapOf(pageable));
    }

    private formatDate(dateToFormat: Date | undefined) {
        if (dateToFormat) {
            const d = moment(dateToFormat);
            return d.format(this.FORMAT_DATE);
        }
        return '';
    }
}
