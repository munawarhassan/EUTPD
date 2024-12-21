import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { DirectivesModule } from '@devacfr/bootstrap';
import { MarketSymbolComponent } from './market-symbol.component';

@NgModule({
    imports: [CommonModule, DirectivesModule],
    exports: [MarketSymbolComponent],
    declarations: [MarketSymbolComponent],
    providers: [],
})
export class MarketSymbolModule {}
