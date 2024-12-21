import { HttpClient } from '@angular/common/http';
import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Pipe({
    name: 'auth',
})
export class AuthPipe implements PipeTransform {
    constructor(private httpClient: HttpClient, private domSanitizer: DomSanitizer) {}

    transform(url: string): Observable<string> {
        return (
            this.httpClient
                // load the image as a blob
                .get(url, { responseType: 'blob' })
                // create an object url of that blob that we can use in the src attribute
                .pipe(map((e) => URL.createObjectURL(e)))
        );
    }
}
