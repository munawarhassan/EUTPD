import { ChangeDetectorRef, Directive, ElementRef, HostBinding, Input, OnChanges, SimpleChanges } from '@angular/core';
import { SubmissionStatus } from '@devacfr/euceg';
import { ClassBuilder } from '@devacfr/util';

@Directive({
    // eslint-disable-next-line @angular-eslint/directive-selector
    selector: '[submissionStatus]',
})
export class SubmissionStatusDirective implements OnChanges {
    @Input()
    public status: SubmissionStatus | undefined;

    @Input()
    public statusSize: 'lg' | 'sm' | undefined;

    @HostBinding('class')
    public get class() {
        if (!this.status) return '';
        const builder = ClassBuilder.create('badge');
        if (this.statusSize) builder.flag('badge-', this.statusSize);
        switch (this.status) {
            case 'NOT_SEND':
            case 'PENDING':
                builder.css('badge-light-dark');
                break;
            case 'SUBMITTING':
                builder.css('badge-light-primary');
                break;
            case 'SUBMITTED':
                builder.css('badge-light-success');
                break;
            case 'ERROR':
                builder.css('badge-light-danger');
                break;
            case 'CANCELLED':
                builder.css('badge-light-warning');
                break;
        }
        return builder.toString();
    }

    constructor(private _elementRef: ElementRef, private _cd: ChangeDetectorRef) {}
    ngOnChanges(changes: SimpleChanges): void {
        if (changes.status) {
            const el = this._elementRef.nativeElement as HTMLElement;
            if (changes.status.currentValue) {
                el.innerHTML = changes.status.currentValue;
            } else {
                el.innerHTML = '';
            }
        }
    }
}
