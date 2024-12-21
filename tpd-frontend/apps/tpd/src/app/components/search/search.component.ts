import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { SvgIcons } from '@devacfr/bootstrap';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Component({
    selector: 'app-search',
    templateUrl: './search.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SearchComponent {
    @Input()
    public placeholder = 'Search...';

    @Input()
    public inputClass = 'form-control-solid';

    @Input()
    public width = 'w-md-400px';

    @Output()
    public search = new EventEmitter<string>();

    @Output()
    public cleared = new EventEmitter<void>();

    public formControl: FormGroup;

    constructor(public svgIcons: SvgIcons, private _fb: FormBuilder) {
        this.formControl = this._fb.group({
            searchTerm: [null],
        });
        this.searchTerm.valueChanges
            .pipe(debounceTime(400), distinctUntilChanged())
            .subscribe(() => this.handleSearch());
    }

    public get searchTerm(): FormControl {
        return this.formControl.get('searchTerm') as FormControl;
    }

    public handleSearch() {
        this.search.emit(this.searchTerm.value);
    }

    public clear() {
        this.formControl.reset();
    }

    public handleClear() {
        this.clear();
        this.cleared.emit();
    }
}
