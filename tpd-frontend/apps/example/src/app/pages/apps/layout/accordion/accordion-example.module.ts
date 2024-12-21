import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AccordionModule } from '@devacfr/bootstrap';

import { AccordionExampleComponent } from './accordion-example.component';

@NgModule({
    imports: [
        CommonModule,
        RouterModule.forChild([
            {
                path: '',
                component: AccordionExampleComponent,
            },
        ]),
        AccordionModule,
    ],
    exports: [RouterModule],
    declarations: [AccordionExampleComponent],
    providers: [],
})
export class AccordionExampleModule {}
