import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MenuModule } from '@devacfr/layout';
import { SelectLanguageComponent } from './select-language.component';

@NgModule({
    imports: [CommonModule, MenuModule],
    exports: [SelectLanguageComponent],
    declarations: [SelectLanguageComponent],
    providers: [],
})
export class SelectLanguageModule {}
