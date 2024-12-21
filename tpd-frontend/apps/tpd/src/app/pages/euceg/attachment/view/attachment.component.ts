import { Location } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, HostBinding, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { PermissionsService, UserToken } from '@devacfr/auth';
import { BlockUI, BreadcrumbService, DrawerDirective, SvgIcons } from '@devacfr/bootstrap';
import { AttachmentRequest, AttachmentService, AttachmentUpdate } from '@devacfr/euceg';
import { I18nService, NotifierService } from '@devacfr/layout';
import { Subscription } from 'rxjs';
import { finalize, switchMap } from 'rxjs/operators';
import { AttachmentManager } from '../attachment.manager';

@Component({
    selector: 'app-attachment',
    templateUrl: './attachment.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AttachmentComponent implements OnInit, OnDestroy {
    public AVATAR_VERSION = new Date().getTime();

    @HostBinding('class')
    public class = 'd-block container';

    public attachment: AttachmentRequest | undefined;
    public readOnly: boolean;
    public checkIntegrity = false;

    public selectedWhereUsed: AttachmentRequest | undefined;

    public formControl: FormGroup;

    private _subscriptions = new Subscription();
    private _block = new BlockUI();

    constructor(
        public svgIcons: SvgIcons,
        private _fb: FormBuilder,
        private _cd: ChangeDetectorRef,
        private _route: ActivatedRoute,
        private _router: Router,
        private _location: Location,
        private _notifierService: NotifierService,
        private _i8n: I18nService,
        private _attachmentService: AttachmentService,
        private _attachmentManager: AttachmentManager,
        private _permissionsService: PermissionsService,
        private _userToken: UserToken,
        private _breadcrumbService: BreadcrumbService
    ) {
        this.readOnly = !this._permissionsService.hasAnyAuthority(this._userToken, 'ADMIN');
        this.formControl = this.createForm(this.readOnly);
    }

    public ngOnInit() {
        this.refresh();
    }

    public ngOnDestroy(): void {
        this._subscriptions.unsubscribe();
    }

    public get confidential(): FormControl {
        return this.formControl.get('confidential') as FormControl;
    }

    public refresh(): void {
        this._block.block();
        this._route.paramMap
            .pipe(
                switchMap((params: ParamMap) =>
                    this._attachmentService
                        .show(params.get('attachment') as string)
                        .pipe(finalize(() => this._block.release()))
                )
            )
            .subscribe({
                next: (attachment) => {
                    this._breadcrumbService.set('@attachment', attachment.filename);
                    this.attachment = attachment;
                    this.setValue(attachment);
                    this._cd.detectChanges();
                },
                error: (err) => this._notifierService.error(err),
            });
    }

    public setWhereUsed(attachment: AttachmentRequest, drawer: DrawerDirective) {
        drawer.toggle();
        this.selectedWhereUsed = attachment;
    }

    public save() {
        if (this.formControl.invalid) {
            return;
        }

        const value = this.formControl.value;
        value.confidential = this.confidential.value;
        const attachmentUpdate = value as AttachmentUpdate;
        this._block.block();
        this._attachmentService
            .update(value)
            .pipe(finalize(() => this._block.release()))
            .subscribe({
                next: () => {
                    this._notifierService.success(attachmentUpdate.filename + ' has been successfully updated');
                    this.goBack();
                },
                error: (err) => this._notifierService.error(err),
            });
    }

    public delete(attachment: AttachmentRequest) {
        this._attachmentManager.deleteAttachement(attachment).subscribe((done) => {
            if (done) this.goBack();
        });
    }

    public preview(attachment: AttachmentRequest) {
        this._attachmentManager.preview(attachment.attachmentId);
    }

    public goToRevisions(attachment: AttachmentRequest) {
        this._router.navigate(['../../rev', attachment.attachmentId], { relativeTo: this._route });
    }

    public goBack(): void {
        this._location.back();
    }

    private createForm(disabled: boolean): FormGroup {
        const grp = this._fb.group({
            attachmentId: [null],
            filename: [{ value: '', disabled: disabled }, [Validators.required]],
            confidential: [{ value: '', disabled: disabled }],
            action: [{ value: '', disabled: disabled }],
            sendStatus: [{ value: '', disabled: disabled }],
        });
        const action = grp.get('action') as FormControl;
        const sendStatus = grp.get('sendStatus') as FormControl;
        const confidential = grp.get('confidential') as FormControl;
        this._subscriptions.add(
            confidential.valueChanges.subscribe((value) => {
                if (this.attachment?.confidential !== value) {
                    this.checkIntegrity = true;
                    action.disable({ emitEvent: false });
                    sendStatus.disable({ emitEvent: false });
                } else {
                    this.checkIntegrity = false;
                    action.enable({ emitEvent: false });
                    sendStatus.enable({ emitEvent: false });
                }
            })
        );
        this._subscriptions.add(
            action.valueChanges.subscribe((value) => {
                if (value != this.attachment?.action || sendStatus.value != this.attachment?.sendStatus) {
                    this.checkIntegrity = true;
                    confidential.disable({ emitEvent: false });
                } else {
                    this.checkIntegrity = false;
                    confidential.enable({ emitEvent: false });
                }
            })
        );
        this._subscriptions.add(
            sendStatus.valueChanges.subscribe((value) => {
                if (value != this.attachment?.sendStatus || action.value != this.attachment?.action) {
                    this.checkIntegrity = true;
                    confidential.disable({ emitEvent: false });
                } else {
                    this.checkIntegrity = false;
                    confidential.enable({ emitEvent: false });
                }
            })
        );
        return grp;
    }

    private setValue(attachment: AttachmentRequest): void {
        this.formControl.patchValue(
            {
                attachmentId: attachment.attachmentId,
                filename: attachment.filename,
                confidential: attachment.confidential,
                sendStatus: attachment.sendStatus,
                action: attachment.action,
            },
            { emitEvent: false }
        );
    }
}
