import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DirectivesModule } from '@devacfr/bootstrap';
import { Select2Module } from '@devacfr/forms';
import { LayoutModule, PortletModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { StickyFormActionsComponent } from './sticky-form-actions.component';

@NgModule({
    imports: [
        TranslateModule,
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule.forChild([
            {
                path: '',
                component: StickyFormActionsComponent,
            },
        ]),
        LayoutModule,
        PortletModule,
        DirectivesModule,
        Select2Module,
    ],
    declarations: [StickyFormActionsComponent],
    exports: [RouterModule],
})
export class SticklyPortletViewModule {}
