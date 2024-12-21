import { HttpClient } from '@angular/common/http';
import { Enumerable } from '../utils/enumerable.decorator';
import { isObservable, Observable, of, OperatorFunction } from 'rxjs';
import { map } from 'rxjs/operators';
import { Page, Pageable, PageImpl } from '../paging';
import { HateoasLink, HateoasResponse } from './typing';

// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace Hateoas {
    export function isHateoasResponse(obj: any) {
        return obj != null && obj.content && obj.page && obj.links;
    }

    export function page<T, R extends HateoasResource>(
        httpClient: HttpClient,
        pageable?: Pageable
    ): OperatorFunction<HateoasResponse<T>, Page<R>> {
        return (observer: Observable<HateoasResponse<T>>): Observable<Page<R>> => {
            return observer.pipe(
                map((response) => {
                    return toPage(response, httpClient, pageable);
                })
            );
        };
    }

    export function toPage<T, R extends HateoasResource>(
        response: HateoasResponse<T>,
        httpClient: HttpClient,
        pageable?: Pageable
    ): Page<R> {
        if (!isHateoasResponse(response)) return Page.empty();
        const apage = response.page;
        const content = response.content.map((el) => {
            const rsrc = new HateoasResource() as R;
            rsrc.wrap(httpClient, el);
            return rsrc;
        });
        const sort = pageable ? pageable.sort : [];
        return new PageImpl<R>(
            content,
            Pageable.of(apage.number, apage.size, pageable?.filters, pageable?.search, ...sort),
            apage.totalElements
        );
    }

    export function resource<T, R extends HateoasResource>(httpClient: HttpClient): OperatorFunction<T, R> {
        return (observer: Observable<T>): Observable<R> => {
            return observer.pipe(
                map((response) => {
                    const rsrc = new HateoasResource() as R;
                    rsrc.wrap(httpClient, response);
                    return rsrc;
                })
            );
        };
    }
}

export class HateoasResource {
    @Enumerable(false)
    public links: HateoasLink[] | undefined;

    private _httpClient!: HttpClient;

    public wrap(httpClient: HttpClient, data: unknown): HateoasResource {
        Object.defineProperty(this, '_httpClient', {
            value: httpClient,
            enumerable: false,
            writable: false,
        });
        Object.assign(this, data);
        if (this.links) {
            this.links.forEach((link) => {
                const func = (pageable?: Pageable): Observable<any> => {
                    if (!this._httpClient) {
                        return of();
                    }
                    const property = this.getLink(link);
                    if (property && !pageable) {
                        return of(property);
                    }
                    const options: any = {};
                    if (pageable) {
                        options.params = pageable.httpParams();
                        return this._httpClient
                            ?.get(link.href, options)
                            .pipe(map((el) => this.setLink(link, el, pageable)));
                    }
                    return this._httpClient.get(link.href).pipe(map((el) => this.setLink(link, el)));
                };
                Object.defineProperty(this, link.rel, {
                    value: func,
                    enumerable: false,
                    writable: false,
                });
            });
        }
        return this;
    }

    public load(reference: keyof HateoasResource) {
        const link = this[reference];
        if (isObservable(link)) {
            link.subscribe(
                // eslint-disable-next-line @typescript-eslint/no-empty-function
                () => {}
            );
        }
    }

    private setLink(link: HateoasLink, value: any, pageable?: Pageable): any {
        let val = value;
        if (Hateoas.isHateoasResponse(value) && this._httpClient)
            val = Hateoas.toPage(value as HateoasResponse<any>, this._httpClient, pageable);
        else if (value.content) {
            val = value.content;
        } else {
            val = value;
        }
        if (this.getLink(link)) {
            const obj = this as Record<string, unknown>;
            obj['_' + link.rel] = val;
        } else {
            Object.defineProperty(this, '_' + link.rel, {
                value: val,
                enumerable: false,
                writable: true,
            });
        }
        return val;
    }

    private getLink(link: HateoasLink): unknown {
        const o = this as Record<string, unknown>;
        return o['_' + link.rel];
    }
}
