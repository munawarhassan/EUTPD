import { AfterContentInit, Component, ContentChild, OnInit } from '@angular/core';
import { ContentComponent } from './layout/content/content.component';
import { TopbarDirective } from './layout/topbar/topbar.directive';
import { LayoutService } from './service/layout.service';

@Component({
    selector: 'lt-layout',
    templateUrl: './layout.component.html',
    styleUrls: ['./layout.component.scss'],
})
export class LayoutComponent implements OnInit, AfterContentInit {
    public contentClass = '';

    @ContentChild(ContentComponent)
    private contentComponent: ContentComponent | undefined;

    @ContentChild(TopbarDirective)
    public topbarTemplate: TopbarDirective | undefined;

    constructor(public layout: LayoutService) {}

    ngOnInit(): void {
        document.body.setAttribute('id', 'lt-body');
    }

    ngAfterContentInit(): void {
        if (this.contentComponent) {
            if (this.contentComponent.width === 'fluid') this.contentClass = 'container-fluid';
            if (this.contentComponent.width === 'fixed') this.contentClass = 'container-xxl';
        }
    }
}
