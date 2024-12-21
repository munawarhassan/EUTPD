import { HttpClient, HttpResponse } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { AuthProvider } from '@devacfr/auth';
import { ErrorResponse, FilterTerms, Hateoas, HateoasResponse, Page, Pageable } from '@devacfr/util';
import moment from 'moment';
import { FileUploader } from 'ng2-file-upload';
import { Observable } from 'rxjs';
import { ProductUpdateRequest, SheetDescriptor } from '.';
import { BACKEND_SERVER_API_URL_TOKEN } from '../shared';
import { Euceg } from './euceg';
import { ProductList } from './product-list.model';
import { ProductRequest } from './product-request.model';
import {
    BulkRequest,
    ProductDiffRequest,
    ProductPirStatus,
    ProductRevision,
    ProductRevisionDiffItem,
    ProductType,
    UpdatePirStatusRequest,
} from './typing';

@Injectable({ providedIn: 'root' })
export class ProductService {
    private API_URL: string;

    private static formatDate(date: Date): string {
        if (typeof date != null) {
            return moment(date).format(Euceg.dateFormat);
        }
        return '';
    }

    constructor(
        @Inject(BACKEND_SERVER_API_URL_TOKEN) private BACKEND_SERVER_API_URL: string,
        private _authProvider: AuthProvider,
        private _httpClient: HttpClient
    ) {
        this.API_URL = this.BACKEND_SERVER_API_URL + 'products';
    }

    public findAllNewProduct(productType: ProductType, pageable: Pageable): Observable<Page<ProductList>> {
        return this._httpClient.get<Page<ProductList>>(`${this.API_URL}/findAllNewProduct`, {
            params: {
                productType,
            },
        });
    }

    public page(pageable: Pageable): Observable<Page<ProductList>> {
        return this._httpClient
            .get<HateoasResponse<ProductList>>(this.API_URL, {
                params: pageable.httpParams(),
            })
            .pipe(Hateoas.page(this._httpClient, pageable));
    }

    public whereUsed(uuid: string, pageable: Pageable): Observable<Page<ProductList>> {
        let params = pageable.httpParams();
        params = params.set('uuid', uuid);
        return this._httpClient
            .get<HateoasResponse<ProductList>>(`${this.API_URL}/whereUsed`, {
                params,
            })
            .pipe(Hateoas.page(this._httpClient, pageable));
    }

    public revisions(
        id: string,
        pageable: Pageable,
        range?: { startDate?: Date; endDate?: Date }
    ): Observable<Page<ProductRevision>> {
        return this._httpClient
            .get<Page<ProductRevision>>(`${this.API_URL}/${id}/rev`, {
                params: pageable.httpParams(),
            })
            .pipe(Page.mapOf(pageable));
    }

    public latestRevision(id: string): Observable<ProductRevision> {
        return this._httpClient.get<ProductRevision>(`${this.API_URL}/${id}/rev/latest`);
    }

    public compareRevision(
        productNumber: string,
        revisedRevision: ProductRevision | 'CURRENT',
        originalRevision: ProductRevision
    ): Observable<ProductRevisionDiffItem> {
        return this._httpClient.get<ProductRevisionDiffItem>(`${this.API_URL}/${productNumber}/rev/compare`, {
            params: {
                revised: typeof revisedRevision === 'string' ? revisedRevision : String(revisedRevision.id),
                original: String(originalRevision.id),
            },
        });
    }

    public show(id: string): Observable<ProductRequest> {
        return this._httpClient.get<ProductRequest>(`${this.API_URL}/${id}`).pipe(Hateoas.resource(this._httpClient));
    }

    public create(product: ProductUpdateRequest): Observable<ProductRequest> {
        return this._httpClient.post<ProductRequest>(this.API_URL, product);
    }

    public update(product: ProductUpdateRequest): Observable<ProductRequest> {
        return this._httpClient.put<ProductRequest>(`${this.API_URL}/${product.productNumber}`, product);
    }

    public updatePirStatus(id: string, newStatus: ProductPirStatus): Observable<ProductRequest> {
        return this._httpClient.put<ProductRequest>(`${this.API_URL}/${id}/pirStatus`, {
            productNumber: id,
            newStatus,
        } as UpdatePirStatusRequest);
    }

    public delete(id: string): Observable<void> {
        return this._httpClient.delete<void>(`${this.API_URL}/${id}`);
    }

    public validateProduct(product): Observable<ErrorResponse> {
        return this._httpClient.post<ErrorResponse>(`${this.API_URL}/validate`, this.unwrap(product));
    }

    public getSheets(productType: ProductType): Observable<SheetDescriptor[]> {
        return this._httpClient.get<SheetDescriptor[]>(`${this.API_URL}/${productType}/sheets`);
    }

    public createImportFileUploader(): FileUploader {
        return new FileUploader({
            allowedMimeType: [
                'application/vnd.ms-excel',
                'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
            ],
            removeAfterUpload: false,
            itemAlias: 'files',
            authToken: 'Bearer ' + this._authProvider.getLocalPrincipal()?.token,
            url: `${this.API_URL}/import`,
        });
    }

    public importProducts(
        files: File[],
        productType: ProductType,
        keepSaleHistory = true,
        ...sheets: string[]
    ): Observable<void> {
        const formData = new FormData();
        formData.append('product_type', productType);
        formData.append('sheets', sheets.join('|'));
        formData.append('keep_sale_history', String(keepSaleHistory));
        files.forEach((file) => formData.append('files', file));

        return this._httpClient.post<void>(`${this.API_URL}/import`, formData);
    }

    public diff(
        file: File,
        productType: ProductType,
        keepSaleHistory = true,
        ...sheets: number[]
    ): Observable<ProductDiffRequest> {
        const formData = new FormData();
        formData.append('product_type', productType);
        if (sheets) {
            formData.append('sheets', sheets.join('|'));
        }
        formData.append('keep_sale_history', String(keepSaleHistory));
        formData.append('file', file);

        return this._httpClient.post<ProductDiffRequest>(`${this.API_URL}/diff`, formData);
    }

    public exportToExcel(
        productType: ProductType,
        filters?: FilterTerms
    ): Observable<HttpResponse<Blob | MediaSource>> {
        const request: BulkRequest = {
            action: 'exportExcel',
            filters,
        };
        return this._httpClient.post<Blob | MediaSource>(`${this.API_URL}/${productType}/export`, request, {
            responseType: 'blob' as 'json',
            observe: 'response',
        });
    }

    public unwrap(product: Partial<ProductRequest>): ProductRequest {
        if (product.links) {
            delete product.links;
        }
        if (product.product) {
            if (product.product.Presentations && product.product.Presentations.Presentation) {
                product.product.Presentations.Presentation.forEach((p) => {
                    if (p.LaunchDate != null) {
                        delete p.LaunchDate.date;
                    }
                    if (p.WithdrawalDate != null) {
                        p.WithdrawalDate.value = ProductService.formatDate(p.WithdrawalDate.date);
                        delete p.WithdrawalDate.date;
                    }
                });
            }
        }
        return product as ProductRequest;
    }
}
