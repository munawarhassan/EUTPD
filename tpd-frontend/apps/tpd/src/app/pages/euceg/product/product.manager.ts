import { Injectable } from '@angular/core';
import { ProductRequest, ProductService, ProductUpdateRequest } from '@devacfr/euceg';
import { NotifierService } from '@devacfr/layout';
import { FilterTerms, QueryOperator } from '@devacfr/util';
import { EMPTY, Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class ProductManager {
    constructor(private _productService: ProductService, private _notifierService: NotifierService) {}

    public createOrUpdate(product: ProductRequest | undefined, isNewProduct = false): Observable<ProductRequest> {
        const unwrap = this.unwrap(product);
        if (!unwrap) {
            return EMPTY;
        }
        const request: ProductUpdateRequest = {
            productNumber: unwrap.productNumber,
            product: unwrap.product,
            generalComment: unwrap.generalComment,
            previousProductNumber: unwrap.previousProductNumber,
        };

        if (isNewProduct) {
            // Create
            return this._productService.create(request).pipe(this._notifierService.catchError());
        } else {
            // Update
            return this._productService.update(request).pipe(this._notifierService.catchError());
        }
    }

    private unwrap(product?: Partial<ProductRequest>): ProductRequest | undefined {
        if (product) {
            return this._productService.unwrap(product);
        }
        return undefined;
    }

    public export(product: ProductRequest, callback?: () => void): void {
        const filter: FilterTerms = {
            productNumber: [
                {
                    op: QueryOperator.equals,
                    values: [product.productNumber.toLowerCase()],
                },
            ],
        };
        this._productService
            .exportToExcel(product.productType, filter)
            .pipe(finalize(() => callback && callback()))
            .subscribe({
                next: (response) => {
                    let filename = '';
                    const disposition = response.headers.get('Content-Disposition');
                    if (disposition && disposition.indexOf('attachment') !== -1) {
                        const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
                        const matches = filenameRegex.exec(disposition);
                        if (matches != null && matches[1]) filename = matches[1].replace(/['"]/g, '');
                    }
                    if (response.body) {
                        const urlBlob = URL.createObjectURL(response.body);
                        const a = document.createElement('a');
                        a.href = urlBlob;
                        a.download = filename;
                        a.click();
                    }
                },
                error: (err) => this._notifierService.error(err),
            });
    }
}
