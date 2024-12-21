import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { TagifyModule } from '@devacfr/forms';
import { TagifyGroupComponent } from './tagify-group.component';

@NgModule({
    imports: [CommonModule, TagifyModule],
    exports: [TagifyGroupComponent],
    declarations: [TagifyGroupComponent],
    providers: [],
})
export class TagifyGroupModule {}
