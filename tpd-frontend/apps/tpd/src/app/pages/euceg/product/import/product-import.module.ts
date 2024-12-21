import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthenticateGuard, UserAuthGuard } from '@devacfr/auth';
import { BreadcrumbObject, DirectivesModule, InlineSVGModule, NavModule } from '@devacfr/bootstrap';
import { EucegCoreModule } from '@devacfr/euceg';
import { FormControlModule } from '@devacfr/forms';
import { LayoutModule, MenuModule, PortletModule, TableModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { DiffHtmlModule } from '@tpd/app/components/diff';
import { ProductImportComponent } from './product-import.component';
import { NgScrollbarModule } from 'ngx-scrollbar';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule.forChild([
            {
                path: '',
                component: ProductImportComponent,
                canActivate: [AuthenticateGuard, UserAuthGuard],
                data: {
                    readOnly: false,
                    breadcrumb: {
                        title: 'Import Product',
                        label: 'Import Excel Product File',
                    } as BreadcrumbObject,
                },
            },
        ]),
        TranslateModule,
        // internal
        FormControlModule,
        LayoutModule,
        DirectivesModule,
        PortletModule,
        NgScrollbarModule,
        TableModule,
        NavModule,
        MenuModule,
        InlineSVGModule,
        EucegCoreModule,
        DiffHtmlModule,
    ],
    declarations: [ProductImportComponent],
})
export class ProductImportModule {}
