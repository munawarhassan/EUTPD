import { Component, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { ApplicationStateInfo, SetupService, StartupService } from '@devacfr/core';
import { NotifierService, WizardComponent } from '@devacfr/layout';
import { ElementAnimateUtil } from '@devacfr/util';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-setup',
    templateUrl: './setup.component.html',
    styleUrls: ['./setup.component.scss'],
})
export class SetupComponent {
    public applicationInfo: ApplicationStateInfo | undefined;

    @ViewChild(WizardComponent)
    public wizard!: WizardComponent;

    public data: Record<string, unknown> = {};

    public startStep = 1;

    constructor(
        private _router: Router,
        private _setupService: SetupService,
        private _notifierService: NotifierService,
        private _startupService: StartupService
    ) {
        if (this._setupService.hasSetupDatabase()) {
            this.startStep = 3;
        }
        this._startupService.info().subscribe({
            next: (info) => {
                this.applicationInfo = info;
                if (this.applicationInfo.status !== 'FIRST_RUN') {
                    this._router.navigate(['/']);
                }
            },
            error: (err) => {
                this._notifierService.error(err);
            },
        });
    }

    public handleChange() {
        ElementAnimateUtil.scrollTop(0, 600);
    }

    public handleComplete() {
        Swal.fire({
            title: '',
            text: 'The application has been successfully configured!',
            icon: 'success',
        });
        this._setupService.unmarkDatabaseAsSetup();
        this._router.navigate(['/']);
    }
}
