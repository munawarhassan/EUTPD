import { Component } from '@angular/core';
import { FormControl, FormGroup, NgForm, Validators } from '@angular/forms';

@Component({
    selector: 'app-password-meter-example',
    templateUrl: 'password-meter-example.component.html',
})
export class PasswordMeterExampleComponent {
    public newPassword: string | null = null;
    public otherValue: string | null = null;

    public form = new FormGroup({
        password: new FormControl(null, [Validators.minLength(8)]),
    });

    get password(): FormControl {
        return this.form.get('password') as FormControl;
    }

    public submit(form: NgForm): void {
        if (form.invalid) return;
    }
}
