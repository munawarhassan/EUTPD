import { CommonModule } from '@angular/common';
import { ModuleWithProviders, NgModule } from '@angular/core';
import { DirectivesModule } from '../../directive';
import { InlineSVGModule } from '../inline-svg';
import { AccordionItemComponent } from './accordion-item.component';
import { AccordionComponent } from './accordion.component';
import { AccordionConfig, ACCORDION_CONFIG } from './accordion.config';

@NgModule({
    imports: [CommonModule, InlineSVGModule, DirectivesModule],
    exports: [AccordionComponent, AccordionItemComponent],
    declarations: [AccordionComponent, AccordionItemComponent],
})
export class AccordionModule {
    public static forRoot(config: AccordionConfig = { closeOthers: true }): ModuleWithProviders<AccordionModule> {
        return {
            ngModule: AccordionModule,
            providers: [
                {
                    provide: ACCORDION_CONFIG,
                    useValue: config,
                },
            ],
        };
    }
}
