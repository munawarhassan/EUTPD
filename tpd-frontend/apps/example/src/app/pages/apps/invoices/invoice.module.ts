import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DirectivesModule, InlineSVGModule } from '@devacfr/bootstrap';
import { FlatPickrModule, Select2Module } from '@devacfr/forms';
import { InvoiceCreateComponent } from './invoice-create.component';
import { Invoice1Component } from './view/invoice1.component';
import { Invoice2Component } from './view/invoice2.component';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        RouterModule.forChild([
            {
                path: 'create',
                component: InvoiceCreateComponent,
            },
            {
                path: 'view/invoice1',
                component: Invoice1Component,
            },
            {
                path: 'view/invoice2',
                component: Invoice2Component,
            },
        ]),
        DirectivesModule,
        Select2Module,
        InlineSVGModule,
        FlatPickrModule,
    ],
    exports: [InvoiceCreateComponent, Invoice1Component, Invoice2Component],
    declarations: [InvoiceCreateComponent, Invoice1Component, Invoice2Component],
    providers: [],
})
export class InvoiceModule {}
