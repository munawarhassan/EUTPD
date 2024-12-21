import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { SetupService } from '@devacfr/core';
import { NotifierService, WizardStepComponent } from '@devacfr/layout';
import { lastValueFrom, of } from 'rxjs';

function createGeneralForm(formBuilder: FormBuilder): FormGroup {
    return formBuilder.group({
        language: ['', [Validators.required]],
    });
}

@Component({
    selector: 'app-setup-general',
    templateUrl: './setup-general.component.html',
})
export class SetupGeneralComponent implements OnInit {
    @Input()
    public step: WizardStepComponent | undefined;

    @Input()
    public data: Record<string, unknown> | undefined;

    public form: FormGroup;

    constructor(
        private _formBuilder: FormBuilder,
        private _setupService: SetupService,
        private _notifierService: NotifierService
    ) {
        this.form = createGeneralForm(this._formBuilder);
        this._setupService.getGeneralInformation().subscribe({
            next: (data) => this.language.setValue(data.language),
            error: (err) => this._notifierService.error(err),
        });
    }

    ngOnInit(): void {
        if (this.step) {
            this.step.beforeNext = () => {
                if (this.data) {
                    this.data.general = this.form.value;
                }
                return lastValueFrom(of(true));
            };
        }
    }

    public get generalForm(): FormGroup {
        return this.form;
    }

    public get language(): FormControl {
        return this.form.get('language') as FormControl;
    }
}
