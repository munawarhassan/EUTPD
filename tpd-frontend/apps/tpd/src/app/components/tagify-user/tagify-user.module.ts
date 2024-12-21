import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { TagifyModule } from '@devacfr/forms';

import { TagifyUserComponent } from './tagify-user.component';

@NgModule({
    imports: [CommonModule, TagifyModule],
    exports: [TagifyUserComponent],
    declarations: [TagifyUserComponent],
    providers: [],
})
export class TagifyUserModule {}
