import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthenticateGuard, AuthModule, UserAuthGuard } from '@devacfr/auth';
import { BreadcrumbObject, DirectivesModule, InlineSVGModule, PipesModule } from '@devacfr/bootstrap';
import { EucegCoreModule } from '@devacfr/euceg';
import { FormControlModule, Select2Module } from '@devacfr/forms';
import { LayoutModule, MenuModule, PaginationModule, PortletModule, TableModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { AuditComponentModule } from '@tpd/app/components/audit/audit.module';
import { SearchModule } from '@tpd/app/components/search/search.module';
import { EucegComponentModule } from '@tpd/app/euceg/components/euceg-component.module';
import { MarketSymbolModule } from '@tpd/app/euceg/components/market-symbol/market-symbol.module';
import { ProductFilterModule } from '@tpd/app/euceg/components/product-filter';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { ModalModule } from 'ngx-bootstrap/modal';
import { TooltipModule } from 'ngx-bootstrap/tooltip';
import { EcigProductDesignComponent } from './ecig-product/ecig-product-design.component';
import { EcigProductDetailComponent } from './ecig-product/ecig-product-detail.component';
import { EcigProductEmissionComponent } from './ecig-product/ecig-product-emission.component';
import { EcigProductIngredientComponent } from './ecig-product/ecig-product-ingredient.component';
import { EcigProductPresentationComponent } from './ecig-product/ecig-product-presentation.component';
import { EcigProductSaledataModalComponent } from './ecig-product/ecig-product-saledata-modal.component';
import { EcigProductSaledataComponent } from './ecig-product/ecig-product-saledata.component';
import { EcigProductComponent } from './ecig-product/ecig-product.component';
import { ProductInfoComponent } from './partials/product-info.component';
import { ProductManufacturerComponent } from './partials/product-manufacturer.component';
import { ProductValidationComponent } from './partials/product-validation.component';
import { SiteManufacturerModalComponent } from './partials/site-manufacturer-modal.component';
import { SupplierModalComponent } from './partials/supplier-modal.component';
import { ProductManager } from './product.manager';
import { ProductsComponent } from './products.component';
import { TobaccoProductDetailComponent } from './tobacco-product/tobacco-product-detail.component';
import { TobaccoProductIngredientComponent } from './tobacco-product/tobacco-product-ingredient.component';
import { TobaccoProductOtherEmissionComponent } from './tobacco-product/tobacco-product-other-emission.component';
import { TobaccoProductOtherIngredientComponent } from './tobacco-product/tobacco-product-other-ingredient.component';
import { TobaccoProductPresentationComponent } from './tobacco-product/tobacco-product-presentation.component';
import { TobaccoProductSaledataModalComponent } from './tobacco-product/tobacco-product-saledata-modal.component';
import { TobaccoProductSaledataComponent } from './tobacco-product/tobacco-product-saledata.component';
import { TobaccoProductTncoComponent } from './tobacco-product/tobacco-product-tnco.component';
import { TobaccoProductComponent } from './tobacco-product/tobacco-product.component';

@NgModule({
    imports: [
        // angular
        RouterModule.forChild([
            {
                path: 'TOBACCO',
                redirectTo: 'tobacco-products',
            },
            {
                path: 'tobacco-products',
                children: [
                    {
                        path: '',
                        component: ProductsComponent,
                        canActivate: [AuthenticateGuard, UserAuthGuard],
                        data: {
                            productType: 'TOBACCO',
                            readOnly: true,
                            breadcrumb: {
                                title: 'Product Tobacco Manager',
                                label: 'Product Tobacco List',
                            } as BreadcrumbObject,
                        },
                    },
                    {
                        path: 'import',
                        loadChildren: () => import('./import/product-import.module').then((m) => m.ProductImportModule),
                        canActivate: [AuthenticateGuard, UserAuthGuard],
                        data: {
                            productType: 'TOBACCO',
                        },
                    },
                    {
                        path: 'bulk',
                        loadChildren: () => import('./bulk/product-bulk.module').then((m) => m.ProductBulkModule),
                        canLoad: [AuthenticateGuard],
                        data: {
                            productType: 'TOBACCO',
                        },
                    },
                    {
                        path: 'view/:id',
                        component: TobaccoProductComponent,
                        canActivate: [AuthenticateGuard, UserAuthGuard],
                        data: {
                            readOnly: true,
                            breadcrumb: {
                                title: 'Product Tobacco Detail View',
                                alias: 'product',
                            } as BreadcrumbObject,
                        },
                    },
                    {
                        path: 'edit/:id',
                        component: TobaccoProductComponent,
                        canActivate: [AuthenticateGuard, UserAuthGuard],
                        data: {
                            readOnly: false,
                            breadcrumb: {
                                title: 'Product Tobacco Detail',
                                alias: 'product',
                            } as BreadcrumbObject,
                        },
                    },
                    //   {
                    //     path: 'new',
                    //     component: TobaccoProductComponent,
                    //     canActivate: [AuthenticateGuard, UserAuthGuard],
                    //     data: {
                    //       readOnly: false
                    //     }
                    //   },
                    {
                        path: 'send/:id',
                        loadChildren: () => import('./send/product-send.module').then((m) => m.ProductSendModule),
                        canActivate: [AuthenticateGuard, UserAuthGuard],
                    },
                    {
                        path: 'rev/:id',
                        loadChildren: () =>
                            import('./revision/product-revision.module').then((m) => m.ProductRevisionModule),
                        canLoad: [AuthenticateGuard],
                        data: {
                            productType: 'TOBACCO',
                        },
                    },
                ],
            },
            {
                path: 'ECIGARETTE',
                redirectTo: 'ecig-products',
            },
            {
                path: 'ecig-products',
                children: [
                    {
                        path: '',
                        component: ProductsComponent,
                        canActivate: [AuthenticateGuard, UserAuthGuard],
                        data: {
                            productType: 'ECIGARETTE',
                            readOnly: true,
                            breadcrumb: {
                                title: 'Product Ecigarette Manager',
                                label: 'Product Ecigarette List',
                            } as BreadcrumbObject,
                        },
                    },
                    {
                        path: 'import',
                        loadChildren: () => import('./import/product-import.module').then((m) => m.ProductImportModule),
                        canActivate: [AuthenticateGuard, UserAuthGuard],
                        data: {
                            productType: 'ECIGARETTE',
                        },
                    },
                    {
                        path: 'bulk',
                        loadChildren: () => import('./bulk/product-bulk.module').then((m) => m.ProductBulkModule),
                        canLoad: [AuthenticateGuard],
                        data: {
                            productType: 'ECIGARETTE',
                        },
                    },
                    {
                        path: 'view/:id',
                        component: EcigProductComponent,
                        canActivate: [AuthenticateGuard, UserAuthGuard],
                        data: {
                            readOnly: true,
                            breadcrumb: {
                                title: 'Product Ecigarette Detail View',
                                alias: 'product',
                            } as BreadcrumbObject,
                        },
                    },
                    {
                        path: 'edit/:id',
                        component: EcigProductComponent,
                        canActivate: [AuthenticateGuard, UserAuthGuard],
                        data: {
                            readOnly: false,
                            breadcrumb: {
                                title: 'Product Ecigarette Detail',
                                alias: 'product',
                            } as BreadcrumbObject,
                        },
                    },
                    //   {
                    //     path: 'new',
                    //     component: EcigProductComponent,
                    //     canActivate: [AuthenticateGuard, UserAuthGuard],
                    //     data: {
                    //       readOnly: false
                    //     }
                    //   },
                    {
                        path: 'send/:id',
                        loadChildren: () => import('./send/product-send.module').then((m) => m.ProductSendModule),
                        canActivate: [AuthenticateGuard, UserAuthGuard],
                    },
                    {
                        path: 'rev/:id',
                        loadChildren: () =>
                            import('./revision/product-revision.module').then((m) => m.ProductRevisionModule),
                        canLoad: [AuthenticateGuard],
                        data: {
                            productType: 'ECIGARETTE',
                        },
                    },
                ],
            },
        ]),
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        // external
        TranslateModule,
        BsDropdownModule,
        TooltipModule,
        ModalModule,
        // internal
        EucegCoreModule,
        EucegComponentModule,
        FormControlModule,
        ProductFilterModule,
        PipesModule,
        AuthModule,
        PaginationModule,
        LayoutModule,
        PortletModule,
        TableModule,
        DirectivesModule,
        MenuModule,
        PortletModule,
        InlineSVGModule,
        MarketSymbolModule,
        SearchModule,
        Select2Module,
        PaginationModule,
        AuditComponentModule,
    ],
    exports: [RouterModule],
    declarations: [
        ProductsComponent,
        ProductInfoComponent,
        ProductManufacturerComponent,
        ProductValidationComponent,
        SiteManufacturerModalComponent,
        EcigProductSaledataComponent,
        TobaccoProductSaledataComponent,
        EcigProductSaledataModalComponent,
        TobaccoProductSaledataModalComponent,
        SupplierModalComponent,
        // // tobacco
        TobaccoProductComponent,
        TobaccoProductDetailComponent,
        TobaccoProductIngredientComponent,
        TobaccoProductOtherEmissionComponent,
        TobaccoProductOtherIngredientComponent,
        TobaccoProductPresentationComponent,
        TobaccoProductTncoComponent,
        // // ecig
        EcigProductComponent,
        EcigProductDetailComponent,
        EcigProductEmissionComponent,
        EcigProductDesignComponent,
        EcigProductIngredientComponent,
        EcigProductPresentationComponent,
    ],
    providers: [ProductManager],
})
export class ProductModule {}
