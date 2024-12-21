import { HttpClient } from '@angular/common/http';
import { Directive, ElementRef, HostListener, Input } from '@angular/core';

// eslint-disable-next-line @angular-eslint/directive-selector
@Directive({ selector: '[securedLink]' })
export class SecuredLinkDirective {
    @Input()
    public target: string | undefined;

    @Input()
    public securedLink: string | undefined;

    constructor(private _httpClient: HttpClient, private el: ElementRef) {}

    @HostListener('click', ['$event'])
    public open(event: Event) {
        // Prevent default behavior when clicking a link
        event.preventDefault();

        if (!this.securedLink) return;

        // Get filename from href
        const url = this.securedLink;

        this._httpClient.get(url, { responseType: 'blob', observe: 'response' }).subscribe((response) => {
            let filename = '';
            const disposition = response.headers.get('Content-Disposition');
            if (disposition && (disposition.indexOf('attachment') !== -1 || disposition.indexOf('inline') !== -1)) {
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
        });
    }
}
