import { Location } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { BlockUI, SvgIcons } from '@devacfr/bootstrap';
import { ConfigService, DatabaseSetting, DatabaseType, MaintenanceService, TaskMonitoring } from '@devacfr/core';
import { FormSelectObserver, FormSelectOptionType } from '@devacfr/forms';
import { I18nService, NotifierService } from '@devacfr/layout';
import _ from 'lodash-es';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BehaviorSubject, combineLatest, Observable, Subscription } from 'rxjs';
import { finalize, map, share, shareReplay, tap } from 'rxjs/operators';
import { MaintenanceProgressModalComponent } from '../../maintenance/maintenance-progress.modal.component';

@Component({
    selector: 'app-migration',
    templateUrl: './migration.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MigrationComponent implements OnInit, OnDestroy {
    public selectedDatabaseTypeAction$ = new BehaviorSubject<string | null>(null);
    public selectedDatabaseType$: Observable<DatabaseType | undefined>;
    public databaseTypes$: Observable<DatabaseType[]>;
    public databaseTypesObserver$: FormSelectObserver;
    public taskMonitor: TaskMonitoring | undefined;
    public done = false;
    public testing: 'on' | undefined;
    public testResult: 'SUCCESS' | 'FAILED' | undefined;

    private _block = new BlockUI('#m_portlet_migration');

    public formControl: FormGroup;

    private _subscription = new Subscription();

    constructor(
        public svgIcons: SvgIcons,
        private _fb: FormBuilder,
        private _cd: ChangeDetectorRef,
        private _i18Service: I18nService,
        private _configService: ConfigService,
        private _location: Location,
        private _maintenanceService: MaintenanceService,
        private _modalService: BsModalService,
        private _notiferService: NotifierService
    ) {
        this.formControl = this.createForm(this._fb);
        this.databaseTypes$ = this._configService.getSupportedDatabaseTypes().pipe(
            tap(() => this._block.block()),
            finalize(() => this._block.release()),
            shareReplay()
        );

        this.databaseTypesObserver$ = () => {
            return this.databaseTypes$.pipe(
                map((ar) =>
                    ar.map(
                        (v) =>
                            ({
                                name: v.displayName,
                                value: v.key,
                            } as FormSelectOptionType)
                    )
                )
            );
        };
        this.selectedDatabaseType$ = combineLatest([this.databaseTypes$, this.selectedDatabaseTypeAction$]).pipe(
            map(([databaseTypes, selectedType]) => databaseTypes.find((v) => v.key === selectedType)),
            share()
        );
    }

    ngOnInit(): void {
        this._subscription.add(this.selectedDatabaseType$.subscribe((type) => this.setValue(type)));
        this._configService
            .getDefaultSupportedDatabase()
            .subscribe((type) => this.selectedDatabaseTypeAction$.next(type.key));
    }

    ngOnDestroy(): void {
        this._subscription.unsubscribe();
    }

    public migration() {
        if (this.formControl.invalid) {
            return;
        }

        const connection = this.formControl.value as DatabaseSetting;

        this._maintenanceService.migrate(connection).subscribe({
            next: (task) => {
                this.done = false;
                this.taskMonitor = task;
                this._modalService.show(MaintenanceProgressModalComponent, {
                    initialState: {
                        title: this._i18Service.instant('database.migration.modal.title'),
                        messageStart: this._i18Service.instant('database.migration.modal.message-start'),
                        messageEnd: this._i18Service.instant('database.migration.modal.message-end'),
                    },
                    ignoreBackdropClick: true,
                    keyboard: false,
                });
                this._modalService.onHide.subscribe(() => {
                    this.done = true;
                });
            },
            error: (err) => {
                this.done = true;
                this._notiferService.error(err);
            },
        });
    }

    public test() {
        this.testResult = undefined;
        if (this.formControl.invalid) {
            return;
        }

        this.testing = 'on';
        const connection = this.formControl.value as DatabaseSetting;

        this._maintenanceService.testConnection(connection).subscribe({
            next: () => {
                this.testing = undefined;
                this._cd.markForCheck();
                this.testResult = 'SUCCESS';
            },
            error: (err) => {
                this.testResult = 'FAILED';
                this.testing = undefined;
                this._cd.markForCheck();
                this._notiferService.error(err);
            },
        });
    }

    public goBack(): void {
        this._location.back();
    }

    private createForm(fb: FormBuilder): FormGroup {
        const grp = fb.group({
            type: [null, [Validators.required]],
            hostname: [null, [Validators.required, Validators.maxLength(255)]],
            port: [null, [Validators.required]],
            databaseName: [null, [Validators.required, Validators.maxLength(255)]],
            username: [null, [Validators.required, Validators.maxLength(255)]],
            password: [null],
        });
        const typeCtrl = grp.get('type') as FormControl;
        this._subscription.add(
            typeCtrl.valueChanges.subscribe((value) =>
                this.selectedDatabaseTypeAction$.value !== value ? this.selectedDatabaseTypeAction$.next(value) : _.noop
            )
        );
        return grp;
    }

    private setValue(value: DatabaseType | undefined) {
        const db = value
            ? ({
                  type: value.key,
                  hostname: value.defaultHostName,
                  databaseName: value.defaultHostName,
                  port: value.defaultPort,
                  username: value.defaultUserName,
                  password: '',
              } as DatabaseSetting)
            : undefined;
        if (db) {
            this.formControl.setValue(db);
        }
        this._cd.detectChanges();
    }
}
