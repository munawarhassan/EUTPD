import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { InlineSVGModule, PipesModule } from '@devacfr/bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { ProfileCardComponent } from './profile-card.component';

@NgModule({
    imports: [
        // angular
        CommonModule,
        // external
        TranslateModule,
        // internal
        PipesModule,
        InlineSVGModule,
    ],
    exports: [ProfileCardComponent],
    declarations: [ProfileCardComponent],
    providers: [],
})
export class ProfileCardModule {}
