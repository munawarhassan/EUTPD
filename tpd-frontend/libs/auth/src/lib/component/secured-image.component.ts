import { HttpClient } from '@angular/common/http';
import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { Observable, of, Subject } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';

@Component({
    // eslint-disable-next-line @angular-eslint/component-selector
    selector: 'secured-image',
    template: ` <img [src]="dataUrl$ | async" /> `,
})
export class SecuredImageComponent implements OnChanges {
    // This code block just creates an rxjs stream from the src
    // this makes sure that we can handle source changes
    // or even when the component gets destroyed
    // So basically turn src into src$
    @Input()
    private src: string | undefined;

    private src$ = new Subject<string>();

    // this stream will contain the actual url that our img tag will load
    // everytime the src changes, the previous call would be canceled and the
    // new resource would be loaded
    public dataUrl$ = this.src$.pipe(switchMap((url) => this.loadImage(url)));

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.src.currentValue) {
            this.src$.next(changes.src.currentValue);
        }
    }

    // we need HttpClient to load the image
    constructor(private httpClient: HttpClient, private domSanitizer: DomSanitizer) {}

    private loadImage(url: string | undefined): Observable<SafeUrl> {
        if (!url) {
            return of();
        }
        return (
            this.httpClient
                // load the image as a blob
                .get(url, { responseType: 'blob' })
                // create an object url of that blob that we can use in the src attribute
                .pipe(map((e) => this.domSanitizer.bypassSecurityTrustUrl(URL.createObjectURL(e))))
        );
    }
}
