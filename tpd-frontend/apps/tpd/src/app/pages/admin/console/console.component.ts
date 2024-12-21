import { ChangeDetectorRef, Component, OnDestroy } from '@angular/core';
import { BlockUI, SvgIcons } from '@devacfr/bootstrap';
import { LogEvent, LoggerRequest, LogsService } from '@devacfr/core';
import { NotifierService } from '@devacfr/layout';
import { interval, Subscription } from 'rxjs';
import { finalize } from 'rxjs/operators';

@Component({
    selector: 'app-console',
    templateUrl: './console.component.html',
})
export class ConsoleComponent implements OnDestroy {
    public loggers: LoggerRequest[] = [];
    public logs: LogEvent[] = [];
    public reverse = false;
    public predicate = '';
    public filter = '';

    private poll: Subscription | undefined;

    constructor(
        public svgIcons: SvgIcons,
        private _cd: ChangeDetectorRef,
        private _logsService: LogsService,
        private _notifierService: NotifierService
    ) {}

    public ngOnDestroy(): void {
        // Make sure that the interval is destroyed too
        this.stopPolling();
    }

    public changeLevel(name, level) {
        this._logsService
            .changeLevel({
                name,
                level,
            })
            .subscribe(() => this.loadSetting());
    }

    public refresh() {
        const block = new BlockUI('#m_portlet_console').block();
        this._logsService
            .getLastLog()
            .pipe(finalize(() => block.release()))
            .subscribe({
                next: (data) => {
                    this.logs = data.events;
                    this._cd.markForCheck();
                },
                error: (err) => this._notifierService.error(err),
            });
    }

    public format(message: string) {
        // eslint-disable-next-line no-control-regex
        return message.replace(new RegExp('\n', 'g'), '<br/>');
    }

    public textDecorator(evt: LogEvent) {
        const level = evt.level;
        switch (level) {
            case 'WARN':
                return 'text-warning';
            case 'ERROR':
                return 'text-danger';
            default:
                return 'text-info';
        }
    }

    public handleSelectTab(tab) {
        if (tab.id === 'm_portlet_logs') {
            this.onLoadInProgress();
        } else if (tab.id === 'm_portlet_settings') {
            this.stopPolling();
            this.loadSetting();
        }
    }

    public sortClass(prop) {
        if (this.predicate === prop) {
            return this.reverse ? 'column-sortable-desc' : 'column-sortable-asc';
        }
        return '';
    }

    protected onLoadInProgress() {
        this.refresh();
        this.poll = interval(10000).subscribe(() => {
            this.refresh();
        });
    }

    private loadSetting() {
        this._logsService.findAll().subscribe({
            next: (data) => {
                this.loggers = data;
                this._cd.markForCheck();
            },
            error: (err) => this._notifierService.error(err),
        });
    }

    private stopPolling() {
        if (this.poll) {
            this.poll.unsubscribe();
            this.poll = undefined;
        }
    }
}
