import {
    AfterViewInit,
    Component,
    ElementRef,
    Input,
    OnDestroy,
    OnInit,
    Renderer2,
    Self,
    TemplateRef,
    ViewChild,
} from '@angular/core';
import { FormBuilder, FormGroup, NgControl, Validators } from '@angular/forms';
import { SvgIcons } from '@devacfr/bootstrap';
import { AttachmentRef, AttachmentRequest, Attachments, AttachmentService } from '@devacfr/euceg';
import { Select2Data, Select2Directive, Select2Event, Select2Observer, ValueAccessorBase } from '@devacfr/forms';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { EMPTY, Observable, of, ReplaySubject, Subscription } from 'rxjs';
import { catchError, combineAll, map, mergeAll, switchMap } from 'rxjs/operators';

@Component({
    selector: 'app-list-files',
    templateUrl: './list-files.component.html',
    // changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ListFilesComponent
    extends ValueAccessorBase<Attachments | undefined>
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

    public modalRef?: BsModalRef;

    public options$: Select2Observer<Select2Data>;

    public attachments$: Observable<AttachmentRequest[]>;

    public formControl: FormGroup;

    @ViewChild(Select2Directive, { static: true, read: Select2Directive })
    private select2: Select2Directive | undefined;

    private valueChangeSubject = new ReplaySubject<AttachmentRef[] | undefined>();
    private _subscription = new Subscription();

    constructor(
        public svgIcons: SvgIcons,
        @Self() ngControl: NgControl,
        private _elementRef: ElementRef,
        _renderer: Renderer2,
        private _attachmentService: AttachmentService,
        private _modalService: BsModalService,
        private _fb: FormBuilder
    ) {
        super(ngControl, _renderer);
        this.formControl = this._fb.group({
            attachment: [null, [Validators.required]],
        });
        this.ngControl.valueAccessor = this;
        this.options$ = (event$: Observable<Select2Event>) =>
            event$.pipe(
                switchMap((event: Select2Event) => {
                    if (event.state === 'prefetch') {
                        const val = event.selected[0];
                        return this._attachmentService.show(val); //.pipe(toArray());
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

        this.attachments$ = this.valueChangeSubject.pipe(
            switchMap((atts) => {
                return atts
                    ? of(atts).pipe(
                          mergeAll(),
                          map((att) => this._attachmentService.show(att.attachmentID)),
                          combineAll()
                      )
                    : of([]);
            })
        );

        this.attachments$.subscribe((atts) => console.log(atts));
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

    public trackAttachment(index: number, attachment: AttachmentRequest): string {
        return attachment.attachmentId;
    }

    public openModal(template: TemplateRef<unknown>) {
        this.modalRef = this._modalService.show(template, {
            class: 'modal-dialog-centered',
        });
    }

    public addAttachment() {
        if (this.formControl.invalid) {
            return;
        }
        this.modalRef?.hide();
        const attachment = this.formControl.value;
        if (!this.value) {
            this._value = {
                Attachment: [],
            };
        }
        this.value?.Attachment.push({
            attachmentID: attachment.attachment,
        });
        this.markAsChanged();
        this.markAsTouched();
        this.valueChangeSubject.next(this.value?.Attachment);
    }

    public removeAttachment(attachment: AttachmentRequest) {
        if (!this.value) {
            return;
        }
        const index = this.value.Attachment.findIndex((att) => att.attachmentID === attachment.attachmentId);
        if (index >= 0) this.value.Attachment.splice(index, 1);
        if (this.value.Attachment.length === 0) {
            this._value = undefined;
        }
        this.markAsChanged();
        this.markAsTouched();
        this.valueChangeSubject.next(this.value?.Attachment);
    }

    public override writeValue(obj: Attachments | undefined): void {
        super.writeValue(obj);
        this.valueChangeSubject.next(obj?.Attachment);
    }
}
