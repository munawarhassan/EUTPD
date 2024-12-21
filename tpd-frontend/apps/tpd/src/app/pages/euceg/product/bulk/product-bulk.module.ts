import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthenticateGuard, UserAuthGuard } from '@devacfr/auth';
import { BreadcrumbObject, DirectivesModule, InlineSVGModule, NavModule } from '@devacfr/bootstrap';
import { EucegCoreModule } from '@devacfr/euceg';
import { LayoutModule, MenuModule, PortletModule, TableModule, WizardModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { SearchModule } from '@tpd/app/components/search/search.module';
import { EucegComponentModule } from '@tpd/app/euceg/components/euceg-component.module';
import { MarketSymbolModule } from '@tpd/app/euceg/components/market-symbol';
import { ProductFilterModule } from '@tpd/app/euceg/components/product-filter';
import { ProductBulkComponent } from './product-bulk.component';
import { WidgetProductComponent } from './widget-product.component';
import { FormControlModule } from '@devacfr/forms';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        RouterModule.forChild([
            {
                path: '',
                component: ProductBulkComponent,
                canActivate: [AuthenticateGuard, UserAuthGuard],
                data: {
                    breadcrumb: {
                        title: 'Bulk Product Operation',
                        label: 'Bulk Product Operation',
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
        EucegCoreModule,
        EucegComponentModule,
        WizardModule,
        ProductFilterModule,
        MarketSymbolModule,
        SearchModule,
        FormControlModule,
    ],
    declarations: [
        ProductBulkComponent,
        // ProductBulkSubheaderComponent,
        WidgetProductComponent,
    ],
})
export class ProductBulkModule {}
