import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthenticateGuard, UserAuthGuard } from '@devacfr/auth';
import { BreadcrumbObject, DirectivesModule, InlineSVGModule, NavModule } from '@devacfr/bootstrap';
import { DateRangePickerModule } from '@devacfr/forms';
import { LayoutModule, MenuModule, PortletModule, TableModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { DiffHtmlModule } from '@tpd/app/components/diff';
import { EucegComponentModule } from '@tpd/app/euceg/components/euceg-component.module';
import { ProductRevisionsComponent } from './product-revisions.component';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule.forChild([
            {
                path: '',
                component: ProductRevisionsComponent,
                canActivate: [AuthenticateGuard, UserAuthGuard],
                data: {
                    breadcrumb: {
                        title: 'Product Revision',
                        alias: 'revision',
                    } as BreadcrumbObject,
                },
            },
        ]),
        TranslateModule,
        LayoutModule,
        DirectivesModule,
        PortletModule,
        TableModule,
        NavModule,
        MenuModule,
        InlineSVGModule,
        DiffHtmlModule,
        DateRangePickerModule,
        EucegComponentModule,
    ],
    exports: [],
    declarations: [ProductRevisionsComponent],
    providers: [],
})
export class ProductRevisionModule {}
