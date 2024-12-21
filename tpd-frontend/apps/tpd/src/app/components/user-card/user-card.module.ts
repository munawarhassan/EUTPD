import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { InlineSVGModule, PipesModule } from '@devacfr/bootstrap';
import { UserCardComponent } from './user-card.component';

@NgModule({
    imports: [
        // angular
        CommonModule,
        RouterModule,

        // internal
        PipesModule,
        InlineSVGModule,
    ],
    exports: [UserCardComponent],
    declarations: [UserCardComponent],
    providers: [],
})
export class UserCardModule {}
