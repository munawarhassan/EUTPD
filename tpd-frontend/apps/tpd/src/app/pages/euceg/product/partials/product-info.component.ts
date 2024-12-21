import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { BlockUI, SvgIcons } from '@devacfr/bootstrap';
import { EucegService, ProductRequest, SubmissionList, SubmissionRequest, SubmitterRequest } from '@devacfr/euceg';
import { NotifierService } from '@devacfr/layout';
import { Order, Pageable, PageObserver } from '@devacfr/util';
import { fromNationalMarkets } from '@tpd/app/euceg/components/market-symbol';
import { combineLatest, EMPTY, Observable, ReplaySubject } from 'rxjs';
import { catchError, finalize, switchMap, tap } from 'rxjs/operators';

@Component({
    selector: 'app-product-info',
    templateUrl: './product-info.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductInfoComponent {
    public getCountries = fromNationalMarkets;

    @Input()
    public submitter: SubmitterRequest | undefined;

    @Input()
    public get product(): ProductRequest | undefined {
        return this._product;
    }

    public set product(v: ProductRequest | undefined) {
        this._product = v;
        if (this._product != null) {
            this._productChange.next(this._product);
        }
    }
    public submissionsPage: PageObserver<SubmissionList>;
    public currentPageable: Pageable;

    public tableOptions = SubmissionRequest.tableOptions;

    private _productChange = new ReplaySubject<ProductRequest>();

    private _product: ProductRequest | undefined;

    private _block = new BlockUI('#m_submissions_history');

    constructor(public svgIcons: SvgIcons, public euceg: EucegService, private _notifierService: NotifierService) {
        this.currentPageable = Pageable.of(0, 20, undefined, undefined, Order.of('DESC', 'lastModifiedDate'));

        this.submissionsPage = (obs: Observable<Pageable>) => {
            return combineLatest([this._productChange, obs]).pipe(
                tap(() => this._block.block()),
                switchMap(([product, pageable]) => {
                    if (product.submissions) {
                        return product.submissions(pageable).pipe(finalize(() => this._block.release()));
                    }
                    return EMPTY;
                }),
                catchError((err, caught) => {
                    this._notifierService.error(err);
                    return caught;
                })
            );
        };
    }
}
