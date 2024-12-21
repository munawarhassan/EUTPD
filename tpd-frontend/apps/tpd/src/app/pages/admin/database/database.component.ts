import { Location } from '@angular/common';
import { Component } from '@angular/core';
import { BlockUI, SvgIcons } from '@devacfr/bootstrap';
import { ConfigService, DatabaseConfig, DatabaseConnection } from '@devacfr/core';
import { NotifierService } from '@devacfr/layout';
import { EMPTY, forkJoin, Observable } from 'rxjs';
import { catchError, finalize, map, tap } from 'rxjs/operators';

@Component({
    selector: 'app-database',
    templateUrl: './database.component.html',
})
export class DatabaseComponent {
    public data$: Observable<{ database: DatabaseConfig; connection: DatabaseConnection }>;

    private _block = new BlockUI('#m_portlet_database');
    constructor(
        public svgIcons: SvgIcons,
        private _configService: ConfigService,
        private _location: Location,
        private _notifierService: NotifierService
    ) {
        this.data$ = forkJoin([
            this._configService.getDatabaseSetting(),
            this._configService.getCurrentDatabase(),
        ]).pipe(
            tap(() => this._block.block()),
            finalize(() => this._block.release()),
            catchError((err) => {
                this._notifierService.error(err);
                return EMPTY;
            }),
            map(([database, connection]) => ({
                database,
                connection,
            }))
        );
    }

    public goBack(): void {
        this._location.back();
    }
}
