import { Location } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, OnInit, QueryList, ViewChild, ViewChildren } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BlockUI, BreadcrumbService, ScrollDirective, ScrollOptions, SvgIcons } from '@devacfr/bootstrap';
import { ProductDiffItem, ProductDiffRequest, ProductService, ProductType, SheetDescriptor } from '@devacfr/euceg';
import { I18nService, NotifierService, PortletToolType } from '@devacfr/layout';
import { ErrorApiResponse, ErrorResponse, isErrorApiResponse, isErrorResponse } from '@devacfr/util';
import { OutputFormatType } from 'diff2html/lib/types';
import _ from 'lodash-es';
import { isArray } from 'lodash-es';
import { FileItem, FileUploader } from 'ng2-file-upload';
import { Observable, of } from 'rxjs';
import { catchError, finalize, mergeMap, tap } from 'rxjs/operators';

interface SelectedSheetDescriptor extends SheetDescriptor {
    selected?: boolean;
}

function sheetmap(ar: SelectedSheetDescriptor[]) {
    return ar
        .filter(function (i) {
            return i.selected || i.required;
        })
        .map(function (sheet) {
            return sheet.index;
        });
}

export class ProductDiffContainer {
    public diffs: Map<File, ProductDiffItem[]> = new Map();

    private arDiffs: ProductDiffItem[] | undefined;

    public add(file: File, diff: ProductDiffRequest): void {
        this.diffs.set(file, diff.diffs);
        this.arDiffs = undefined;
    }

    public clear(): void {
        this.diffs.clear();
        this.arDiffs = undefined;
    }

    public get files(): File[] {
        return Array.from(this.diffs.keys());
    }
    public get values(): ProductDiffItem[] {
        if (this.arDiffs == null) {
            this.arDiffs = [];
            this.diffs.forEach((value) => this.arDiffs?.push(...value));
        }
        return this.arDiffs;
    }

    public map(): Map<File, ProductDiffItem[]> {
        return this.diffs;
    }

    public remove(file: File) {
        if (this.diffs.has(file)) {
            this.diffs.delete(file);
        }
        this.arDiffs = undefined;
    }
}

@Component({
    selector: 'app-product-import',
    templateUrl: './product-import.component.html',
    styleUrls: ['./product-import.component.scss'],
})
export class ProductImportComponent implements OnInit {
    @ViewChild('inputFile', { static: true })
    public inputFile!: ElementRef;

    @ViewChildren(ScrollDirective)
    public diffScrollbar!: QueryList<ScrollDirective>;

    public scrollOptions: ScrollOptions = {
        activate: true,
        height: 'auto',
        dependencies: '#lt_header,#lt_footer,app-product-import .portlet-foot',
    };

    public diffRequest = new ProductDiffContainer();
    public selectedDiff: ProductDiffItem | undefined;
    public diffOutputFormat: OutputFormatType = 'side-by-side';
    public titleDiff: string | undefined;

    public productType: ProductType;

    public sheets!: SelectedSheetDescriptor[];
    private _sheetOptions = 'all';

    public uploader: FileUploader;
    public errorResponse: ErrorResponse[] | HttpErrorResponse[] | ErrorResponse | HttpErrorResponse | undefined;

    public keepSaleHistory = true;

    private _block = new BlockUI();

    constructor(
        public svgIcons: SvgIcons,
        private _route: ActivatedRoute,
        private _location: Location,
        private _productService: ProductService,
        private _notifierService: NotifierService,
        private _i8n: I18nService,
        private _breadcrumbService: BreadcrumbService
    ) {
        this.productType = this._route.snapshot.data.productType;
        this.uploader = this._productService.createImportFileUploader();
        this.uploader.onAfterAddingFile = this.onSelectFile.bind(this);

        this.uploader.onCompleteItem = (item: FileItem, response: string, status: number): void => {
            if (status < 200 || status > 210) {
                this.errorResponse = JSON.parse(response);
            }
        };
        this.uploader.onCompleteAll = () => {
            if (!this.errorResponse) {
                this._location.back();
                this._notifierService.success('The import was successful');
            } else {
                if (isArray(this.errorResponse)) {
                    this.errorResponse.forEach((err) => {
                        this._notifierService.error(err);
                    });
                } else {
                    this._notifierService.error(this.errorResponse);
                }
                this.errorResponse = undefined;
            }
            this._block.release();
        };
    }
    ngOnInit(): void {
        this._productService.getSheets(this.productType).subscribe((sheets) => {
            this.sheets = sheets.map((sheet) => ({ ...sheet, selected: sheet.required }));
        });
    }

    public get sheetOptions(): string {
        return this._sheetOptions;
    }

    public set sheetOptions(value: string) {
        this._sheetOptions = value;
        if (this._sheetOptions == 'all') {
            this.sheets?.forEach((s) => (!s.required ? (s.selected = false) : _.noop));
        }
    }

    public openFileDialog(): void {
        const input = this.inputFile.nativeElement as HTMLInputElement;
        input.click();
    }

    public onErrorItem(event: { fileItem: FileItem; response: string }): void {
        const error = JSON.parse(event.response);
        if (isErrorApiResponse(error)) {
            const err = error as ErrorApiResponse;
            event.fileItem['errorResponse'] = err.errors.map((e) => e.message).join(',');
        } else if (isErrorResponse(error)) {
            const err = error as ErrorResponse;
            event.fileItem['errorResponse'] = err.failures.map((desc) => desc.description).join(',');
        } else {
            event.fileItem['errorResponse'] = event.response;
        }
    }

    public onRemoveFileItem(event: { fileItem: FileItem }): void {
        this.diffRequest.remove(event.fileItem._file);
    }

    public onSelectFile(file: FileItem | undefined) {
        if (file == undefined) return;
        this.sheetOptions = 'all';

        this._block.block();
        this.updateDiff$(file)
            .pipe(finalize(() => this._block.release()))
            .subscribe(_.noop);
    }

    public updateDiff() {
        this.diffRequest.clear();
        of(...this.uploader.queue)
            .pipe(mergeMap((f) => this.updateDiff$(f)))
            .subscribe(_.noop);
    }

    public updateDiff$(file: FileItem | undefined): Observable<ProductDiffRequest> {
        if (file == undefined) {
            return of();
        }
        const selection = this.sheetOptions === 'all' ? [] : sheetmap(this.sheets);
        return this._productService.diff(file._file, this.productType, this.keepSaleHistory, ...selection).pipe(
            tap((diff) => {
                this.diffRequest.add(file._file, diff);
                this.selectedDiff = undefined;
                if (this.diffRequest && this.diffRequest.diffs && this.diffRequest.diffs.size > 0) {
                    this.onSelectDiff(this.diffRequest.values[0]);
                }
            }),
            catchError((err) => {
                file.isError = true;
                this._notifierService.error(err.error);
                if (isErrorApiResponse(err.error)) {
                    const error = err.error as ErrorApiResponse;
                    file['errorResponse'] = error.errors.map((desc) => desc.message);
                } else if (isErrorResponse(err.error)) {
                    const error = err.error as ErrorResponse;
                    file['errorResponse'] = error.failures.map((desc) => desc.description);
                }
                return of<ProductDiffRequest>();
            })
        );
    }

    public onKeepSaleHistory() {
        this.uploader.queue.forEach((f) => this.onSelectFile(f));
    }

    public onSelectDiff(diff: ProductDiffItem) {
        this.selectedDiff = diff;
        this.titleDiff = diff.productNumber;
    }

    public onSheetOptionsChange() {
        this.updateDiff();
    }

    public onSelectedSheetChange() {
        this.updateDiff();
    }

    public onFullscreen(event: { type?: PortletToolType; state?: string }) {
        // const el = this.diffScrollbar.nativeElement as HTMLElement;
        // // adjust the scroller with fullscreen
        // if (event.state === 'off') {
        //     el.style.height = '60vh';
        // } else {
        //     el.style.height = 'auto';
        // }
    }

    public handleSelectDiffTab() {
        setTimeout(() => this.diffScrollbar.forEach((cmpt) => cmpt.update()), 200);
    }

    public importExcel() {
        if (!this.sheets) {
            return;
        }
        this._block.block();
        const selection = this.sheetOptions === 'all' ? null : sheetmap(this.sheets);
        if (this.uploader.queue.length) {
            this.uploader.onBuildItemForm = (fileItem: FileItem, formData: FormData) => {
                formData.append('product_type', this.productType);
                if (selection) {
                    formData.append('sheets', selection.join('|'));
                }
                formData.append('keep_sale_history', String(this.keepSaleHistory));
            };
            this.uploader.uploadAll();
        }
    }

    public goBack() {
        this._location.back();
    }
}
