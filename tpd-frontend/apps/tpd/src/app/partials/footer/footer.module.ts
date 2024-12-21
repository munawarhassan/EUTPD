import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { DirectivesModule } from '@devacfr/bootstrap';

import { FooterComponent } from './footer.component';

@NgModule({
    imports: [CommonModule, DirectivesModule],
    exports: [FooterComponent],
    declarations: [FooterComponent],
    providers: [],
})
export class FooterModule {}
