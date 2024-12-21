import { ChangeDetectorRef, Component } from '@angular/core';
import { Router } from '@angular/router';
import { SvgIcons } from '@devacfr/bootstrap';
import { MaintenanceService } from '@devacfr/core';
import { I18nService, NotifierService } from '@devacfr/layout';
import { BsModalService } from 'ngx-bootstrap/modal';
import { MaintenanceProgressModalComponent } from '../maintenance/maintenance-progress.modal.component';

@Component({
    selector: 'app-indexing',
    templateUrl: './indexing.component.html',
})
export class IndexingComponent {
    public acknowledge = false;
    public updating = false;
    public errorMessage: string | undefined = undefined;

    constructor(
        public svgIcons: SvgIcons,
        private _maintenanceService: MaintenanceService,
        private _modalService: BsModalService,
        private _i18Service: I18nService,
        private _notiferService: NotifierService,
        private _router: Router,
        private _cd: ChangeDetectorRef
    ) {}

    public reindex() {
        this.updating = true;
        this.errorMessage = undefined;
        this._maintenanceService.index().subscribe({
            next: () => {
                this._modalService.show(MaintenanceProgressModalComponent, {
                    initialState: {
                        title: this._i18Service.instant('indexing.modal.title'),
                        messageStart: this._i18Service.instant('indexing.modal.message-start'),
                        messageEnd: this._i18Service.instant('indexing.modal.message-end'),
                    },
                    ignoreBackdropClick: true,
                    keyboard: false,
                });
                this._modalService.onHide.subscribe(
                    () => {
                        this.acknowledge = true;
                        this._cd.markForCheck();
                    },
                    (err) => {
                        this.acknowledge = false;
                        this.updating = false;
                        this._notiferService.extractErrorMessage(err).then((msg) => (this.errorMessage = msg));
                        this._cd.markForCheck();
                    }
                );
            },
            error: (err) => {
                this.acknowledge = false;
                this.updating = false;
                this._notiferService.extractErrorMessage(err).then((msg) => (this.errorMessage = msg));
                this._cd.markForCheck();
            },
        });
    }

    public goBack(): void {
        this._router.navigate(['/admin']);
    }
}
