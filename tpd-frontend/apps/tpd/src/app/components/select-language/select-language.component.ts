import {
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    OnChanges,
    Output,
    SimpleChanges,
} from '@angular/core';
import { I18nService, LanguageFlag } from '@devacfr/layout';

@Component({
    selector: 'app-select-language',
    templateUrl: './select-language.component.html',
    styleUrls: ['./select-language.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SelectLanguageComponent implements OnChanges {
    @Input()
    public lang: string | undefined;

    public currentLanguage: LanguageFlag | undefined;
    public languages: LanguageFlag[];

    @Output()
    public changed = new EventEmitter<string>();

    constructor(private _I18nService: I18nService) {
        this.languages = this._I18nService.getLanguageFlags();
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.lang && changes.lang.currentValue != changes.lang.previousValue) {
            this.adjustLanguageList(changes.lang.currentValue);
        }
    }

    public setLanguage(event: Event, lang: string) {
        event.preventDefault();
        this.adjustLanguageList(lang);
        this._I18nService.use(lang);
        this.changed.emit(lang);
    }

    private adjustLanguageList(lang: string) {
        this.languages.forEach((language: LanguageFlag) => {
            if (language.lang === lang) {
                language.active = true;
                this.currentLanguage = language;
            } else {
                language.active = false;
            }
        });
    }
}
