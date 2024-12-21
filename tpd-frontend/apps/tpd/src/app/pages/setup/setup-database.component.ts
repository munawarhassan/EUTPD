import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { BlockUI } from '@devacfr/bootstrap';
import { DatabaseSetting, DatabaseType, SetupService } from '@devacfr/core';
import { FormSelectObserver, FormSelectOptionType } from '@devacfr/forms';
import { NotifierService, WizardStepComponent } from '@devacfr/layout';
import _ from 'lodash-es';
import { BehaviorSubject, combineLatest, concat, lastValueFrom, Observable, of, Subscription } from 'rxjs';
import { catchError, finalize, map, share, shareReplay, switchMap, tap } from 'rxjs/operators';

function createDatabaseTypeForm(formBuilder: FormBuilder): FormGroup {
    return formBuilder.group({
        genderDatabase: ['internal'],
    });
}

@Component({
    selector: 'app-setup-database',
    templateUrl: './setup-database.component.html',
})
export class SetupDatabaseComponent implements OnInit, OnDestroy {
    @Input()
    public step: WizardStepComponent | undefined;

    @Input()
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    public data: Record<string, any> | undefined;

    public selectedDatabaseTypeAction$ = new BehaviorSubject<string | undefined>(undefined);
    public selectedDatabaseType$: Observable<DatabaseType | undefined>;
    public databaseTypes$: Observable<DatabaseType[]>;
    public databaseTypesObserver$: FormSelectObserver;

    public form: FormGroup;
    public testResult: 'SUCCESS' | 'FAILED' | undefined;
    public testing: 'on' | undefined;

    private _subscription = new Subscription();

    private _block = new BlockUI();

    constructor(
        private _formBuilder: FormBuilder,
        private _setupService: SetupService,
        private _notifierService: NotifierService
    ) {
        this.form = this.createForm(this._formBuilder);
        this.databaseTypes$ = this._setupService.getDatabaseTypes().pipe(
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

    public ngOnInit() {
        this._subscription.add(this.selectedDatabaseType$.subscribe((type) => this.setValue(type)));
        if (this.step) {
            this.step.beforeNext = () => {
                if (!this.internal && this.database.invalid) {
                    return lastValueFrom(of(false));
                }
                if (this.data) {
                    if (!this.internal) {
                        this.data.database = { ...this.database.value, internal: this.internal };
                    } else {
                        this.data.database = { internal: this.internal };
                    }
                }
                return this.configureDatabase();
            };
            this.step.valid = () => this.valid();
        }

        this._setupService.getDefaultExternalDatabase().subscribe({
            next: (type) => this.selectedDatabaseTypeAction$.next(type.key),
            error: (err) => this._notifierService.error(err),
        });
    }

    ngOnDestroy(): void {
        this._subscription.unsubscribe();
    }

    public get databaseType(): FormGroup {
        return this.form.get('databaseType') as FormGroup;
    }
    public get genderDatabase(): FormControl {
        return this.form.get('databaseType.genderDatabase') as FormControl;
    }

    public get database(): FormGroup {
        return this.form.get('database') as FormGroup;
    }
    public get type(): FormControl {
        return this.form.get('database.type') as FormControl;
    }
    public get hostname(): FormControl {
        return this.form.get('database.hostname') as FormControl;
    }
    public get port(): FormControl {
        return this.form.get('database.port') as FormControl;
    }
    public get databaseName(): FormControl {
        return this.form.get('database.databaseName') as FormControl;
    }
    public get username(): FormControl {
        return this.form.get('database.username') as FormControl;
    }
    public get password(): FormControl {
        return this.form.get('database.password') as FormControl;
    }

    public valid(): boolean {
        const internal = this.genderDatabase.value === 'internal';
        return internal || this.database.valid;
    }

    public get internal(): boolean {
        return this.genderDatabase.value === 'internal';
    }
    public async configureDatabase(): Promise<boolean> {
        const connection = this.database.value as DatabaseSetting;

        this._block.block();
        return await lastValueFrom(
            concat(
                this._setupService.saveDatabaseConfiguration(connection, this.internal),
                this._setupService.progress()
            ).pipe(
                switchMap(() => {
                    this._setupService.markDatabaseAsSetup();
                    return of(true);
                }),
                catchError((err) => {
                    if (err.status && err.status === 404) return of(true);
                    else {
                        this._notifierService.error(err);
                        return of(false);
                    }
                }),
                finalize(() => this._block.release())
            )
        );
    }

    public test() {
        this.testResult = undefined;
        const internal = this.genderDatabase.value === 'internal';
        if (!internal && this.database.invalid) {
            return;
        }
        this.testing = 'on';

        const connection = this.database.value as DatabaseSetting;

        this._setupService.testDatabaseConnection(connection).subscribe({
            next: () => {
                this.testing = undefined;
                this.testResult = 'SUCCESS';
            },
            error: (err) => {
                this.testResult = 'FAILED';
                this.testing = undefined;
                this._notifierService.error(err);
            },
        });
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
            this.database.setValue(db);
        }
    }

    private createForm(fb: FormBuilder): FormGroup {
        const grp = fb.group({
            databaseType: createDatabaseTypeForm(this._formBuilder),
            database: DatabaseSetting.createFormGroup(this._formBuilder),
        });
        const typeCtrl = grp.get('database.type') as FormControl;
        this._subscription.add(
            typeCtrl.valueChanges.subscribe((value) =>
                this.selectedDatabaseTypeAction$.value !== value ? this.selectedDatabaseTypeAction$.next(value) : _.noop
            )
        );
        return grp;
    }
}
