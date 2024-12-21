import { Component, HostBinding, Input } from '@angular/core';

@Component({
    selector: 'app-not-found',
    templateUrl: './not-found.component.html',
    styleUrls: ['./not-found.component.scss'],
})
export class PageNotFoundComponent {
    // full background image
    // full background image
    @Input()
    public image: string;
    // error code, some error types template has it
    @Input()
    public code = '404';
    // error title
    // error title
    @Input()
    public title: string;
    // error subtitle, some error types template has it
    // error subtitle, some error types template has it
    @Input()
    public subtitle: string;
    // error descriptions
    @Input()
    public desc = 'Oops! Something went wrong!';
    // return back button title
    @Input()
    public return = 'Return back';

    @HostBinding('class')
    public classes = 'd-flex flex-column flex-root vh-100';

    constructor() {
        this.code = '404';
        this.title = 'How did you get here';
        this.subtitle = "Sorry we can't seem to find the page you're looking for.";
        this.desc =
            'There may be amisspelling in the URL entered,<br>' +
            'or the page you are looking for may no longer exist.';
        this.image = './assets/media/error/bg3.jpg';
    }
}
