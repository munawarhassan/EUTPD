import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthenticateGuard, UserAuthGuard } from '@devacfr/auth';
import { BreadcrumbObject, DirectivesModule, InlineSVGModule } from '@devacfr/bootstrap';
import { EucegCoreModule } from '@devacfr/euceg';
import { FormControlModule } from '@devacfr/forms';
import { LayoutModule, PortletModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { EucegComponentModule } from '@tpd/app/euceg/components/euceg-component.module';
import { ProductSendComponent } from './product-send.component';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        RouterModule.forChild([
            {
                path: '',
                component: ProductSendComponent,
                canActivate: [AuthenticateGuard, UserAuthGuard],
                data: {
                    breadcrumb: {
                        title: 'Send Product',
                        alias: 'product',
                    } as BreadcrumbObject,
                },
            },
        ]),
        TranslateModule,
        LayoutModule,
        DirectivesModule,
        PortletModule,
        InlineSVGModule,
        FormControlModule,
        EucegCoreModule,
        EucegComponentModule,
    ],
    exports: [],
    declarations: [ProductSendComponent],
    providers: [],
})
export class ProductSendModule {}
