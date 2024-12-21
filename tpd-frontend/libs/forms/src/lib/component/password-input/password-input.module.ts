import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { PasswordInputComponent } from './password-input.component';

@NgModule({
    imports: [CommonModule, FormsModule],
    exports: [PasswordInputComponent],
    declarations: [PasswordInputComponent],
    providers: [],
})
export class PasswordInputModule {}
