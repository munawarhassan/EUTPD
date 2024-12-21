import { Location } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { BlockUI, SvgIcons } from '@devacfr/bootstrap';
import { ConfigService } from '@devacfr/core';
import { NotifierService } from '@devacfr/layout';
import { finalize } from 'rxjs/operators';

@Component({
    selector: 'app-ldap-settings',
    templateUrl: './ldap.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LdapComponent implements OnInit {
    public oneAtATime = true;
    public testConnection: boolean | undefined;
    public testResult: 'SUCCESS' | 'FAILED' | undefined;

    public data: any = {};

    private _block = new BlockUI('#m_portlet_ldap');
    constructor(
        public svgIcons: SvgIcons,
        private _location: Location,
        private _cd: ChangeDetectorRef,
        private _configService: ConfigService,
        private _notifierService: NotifierService
    ) {}

    ngOnInit() {
        this.refresh();
    }

    public refresh() {
        this._block.block();
        this._configService
            .getLdapSetting()
            .pipe(finalize(() => this._block.release()))
            .subscribe({
                next: (config) => {
                    this.data = config;
                    this._cd.markForCheck();
                },
                error: (resp) => {
                    this._notifierService.error(resp);
                },
            });
    }

    public save(form: NgForm): void {
        if (form.invalid) {
            return;
        }
        this._block.block();
        this._configService
            .saveLdapSetting(this.data)
            .pipe(finalize(() => this._block.release()))
            .subscribe({
                next: () => {
                    this._notifierService.success('The LDAP User Directory has been updated');
                    this.goBack();
                },
                error: (resp) => {
                    this._notifierService.error(resp);
                },
            });
    }

    public goBack(): void {
        this._location.back();
    }

    public onDirectoryTypeChanged() {
        this.data.authenticationOnly = this.data.directoryType && this.data.directoryType.startsWith('Internal');
        this._cd.markForCheck();
    }

    public test(form: NgForm) {
        if (form.invalid) {
            return;
        }
        this.testConnection = true;

        this._configService.ldapTestConnection(this.data).subscribe({
            next: () => {
                this.testConnection = false;
                this.testResult = 'SUCCESS';
                this._cd.markForCheck();
            },
            error: (err) => {
                this.testResult = 'FAILED';
                this.testConnection = false;
                this._notifierService.error(err);
                this._cd.markForCheck();
            },
        });
    }
}
