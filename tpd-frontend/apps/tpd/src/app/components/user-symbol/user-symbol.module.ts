import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { DirectivesModule } from '@devacfr/bootstrap';

import { UserSymbolComponent } from './user-symbol.component';

@NgModule({
    imports: [CommonModule, DirectivesModule],
    exports: [UserSymbolComponent],
    declarations: [UserSymbolComponent],
    providers: [],
})
export class UserSymbolModule {}
