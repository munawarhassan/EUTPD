import {
    AfterViewInit,
    Component,
    ElementRef,
    Input,
    OnDestroy,
    OnInit,
    Renderer2,
    Self,
    ViewChild,
} from '@angular/core';
import { NgControl, Validators } from '@angular/forms';
import { AttachmentService, AttachmentRef } from '@devacfr/euceg';
import { Select2Data, Select2Directive, Select2Event, Select2Observer, ValueAccessorBase } from '@devacfr/forms';
import { isArray } from 'lodash-es';
import { EMPTY, Observable, Subscription } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';

@Component({
    selector: 'app-one-file',
    templateUrl: './one-file.component.html',
})
export class OneFileComponent
    extends ValueAccessorBase<AttachmentRef | undefined>
    implements OnInit, AfterViewInit, OnDestroy
{
    @Input()
    public label: string | undefined;

    @Input()
    public placeholder: string | undefined;

    @Input()
    public tooltip: string | undefined;

    @Input()
    public required = false;

    public options$: Select2Observer<Select2Data>;

    @ViewChild(Select2Directive, { static: true, read: Select2Directive })
    private select2: Select2Directive | undefined;

    private _subscription = new Subscription();

    constructor(
        @Self() ngControl: NgControl,
        private _elementRef: ElementRef,
        _renderer: Renderer2,
        private _attachmentService: AttachmentService
    ) {
        super(ngControl, _renderer);
        this.ngControl.valueAccessor = this;
        this.options$ = (event$: Observable<Select2Event>) =>
            event$.pipe(
                switchMap((event: Select2Event) => {
                    if (event.state === 'prefetch') {
                        const val = event.selected[0];
                        if (val) {
                            return this._attachmentService.show(val);
                        }
                        return EMPTY;
                    } else if (event.state === 'search') {
                        if (event.termMatch) {
                            return this._attachmentService
                                .findAttachmentByFileName(event.termMatch)
                                .pipe(catchError(() => EMPTY));
                        }
                    }
                    return EMPTY;
                })
            );
    }

    ngOnInit(): void {
        this._subscription.add(
            this.ngControl.control?.statusChanges?.subscribe(() => {
                // update angular validation class in alt input element
                this.select2?.synchronizeStatusChange(this._elementRef);
            })
        );
        if (!this.required) {
            this.required = this.hasValidator(Validators.required);
        }
    }

    ngAfterViewInit(): void {
        this.select2?.synchronizeStatusChange(this._elementRef);
    }

    ngOnDestroy(): void {
        this._subscription.unsubscribe();
    }

    public handleSelectValueChange(value: string | (string | undefined)[] | undefined) {
        if (!isArray(value)) {
            if (value) {
                this.value = { attachmentID: value };
            } else {
                this.value = undefined;
            }
        }
    }

    public override writeValue(value: AttachmentRef | undefined): void {
        super.writeValue(value);
        this.select2?.writeValue(value?.attachmentID);
    }
}
