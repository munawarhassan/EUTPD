import { Location } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { BlockUI, BreadcrumbService, SvgIcons } from '@devacfr/bootstrap';
import {
    EucegService,
    ProductPirStatus,
    ProductRequest,
    ProductService,
    SubmitterRequest,
    SubmitterService,
} from '@devacfr/euceg';
import { I18nService, NotifierService } from '@devacfr/layout';
import { EMPTY } from 'rxjs';
import { finalize, switchMap, tap } from 'rxjs/operators';
import { ProductManager } from '../product.manager';

@Component({
    selector: 'app-ecig-product',
    templateUrl: './ecig-product.component.html',
})
export class EcigProductComponent implements OnInit {
    public product: ProductRequest | undefined;
    public submitter: SubmitterRequest | undefined;
    public isNewProduct = false;
    public isReadOnly = false;

    private _block = new BlockUI();

    constructor(
        public svgIcons: SvgIcons,
        public euceg: EucegService,
        private _route: ActivatedRoute,
        private _router: Router,
        private _location: Location,
        private _productService: ProductService,
        private _productManager: ProductManager,
        private _submitterService: SubmitterService,
        private _cd: ChangeDetectorRef,
        private _notifierService: NotifierService,
        private _i8n: I18nService,
        private _breadcrumbService: BreadcrumbService
    ) {
        this.isReadOnly = this._route.snapshot.data.readOnly;
    }

    public ngOnInit() {
        this.refresh();
    }

    public refresh(): void {
        this._route.paramMap
            .pipe(
                switchMap((params: ParamMap) => {
                    if (params.has('id')) {
                        this._block.block();
                        this.isNewProduct = false;
                        return this._productService.show(params.get('id') as string).pipe(
                            tap((product) => {
                                this._breadcrumbService.set('@product', product.productNumber);
                                this._submitterService
                                    .show(product.submitterId)
                                    .subscribe((submitter) => (this.submitter = submitter));
                            }),
                            finalize(() => this._block.release())
                        );
                    } else {
                        this.isNewProduct = true;
                        return EMPTY;
                    }
                })
            )
            .subscribe({
                next: (product) => {
                    this.product = this.wrap(product);
                },
                error: (err) => this._notifierService.error(err),
            });
    }

    // Save
    public save() {
        this._block.block();
        this._productManager
            .createOrUpdate(this.product, this.isNewProduct)
            .pipe(finalize(() => this._block.release()))
            .subscribe(() => {
                if (this.isNewProduct) {
                    this._notifierService.success('E-Cigarette product has been created.');
                } else {
                    this._notifierService.success('E-Cigarette product has been updated.');
                }
                this.goBack();
            });
    }

    private wrap(product: ProductRequest): ProductRequest {
        return product;
    }

    public goBack(): void {
        this._location.back();
    }

    public export(product: ProductRequest): void {
        this._block.block();
        this._productManager.export(product, () => this._block.release());
    }

    public send(product: ProductRequest): void {
        this._router.navigate(['/product', product.productType, 'send', product.id]);
    }

    public revision(product: ProductRequest): void {
        this._router.navigate(['/product', product.productType, 'rev', product.id]);
    }

    public pirStatusChanged(status: ProductPirStatus) {
        if (this.product) {
            this.product.pirStatus = status;
        }
    }
}
