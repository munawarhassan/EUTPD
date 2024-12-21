import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    ElementRef,
    HostListener,
    Input,
    OnInit,
    Renderer2,
} from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { ClassBuilder } from '@devacfr/util';
import { TranslateService } from '@ngx-translate/core';
import { filter } from 'rxjs/operators';
import { BaseMenuComponent } from './base-menu.component';
import { MenuItem, MenuRootItemType } from './menu-config';

@Component({
    selector: 'lt-menu',
    templateUrl: './menu.component.html',
    styleUrls: ['./menu.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    // eslint-disable-next-line @angular-eslint/no-inputs-metadata-property
    inputs: ['menuType', 'options'],
})
export class MenuComponent extends BaseMenuComponent implements OnInit {
    @Input()
    public items: MenuRootItemType[] | undefined;

    constructor(
        public activedRoute: ActivatedRoute,
        public router: Router,
        protected _element: ElementRef,
        protected renderer: Renderer2,
        private _cd: ChangeDetectorRef,
        private _translateService: TranslateService
    ) {
        super(_element, renderer);
        this.subscriptions.add(
            this.router.events.pipe(filter((event) => event instanceof NavigationEnd)).subscribe(() => {
                this._cd.markForCheck();
            })
        );
        this.subscriptions.add(_translateService.onLangChange.subscribe(() => this._cd.markForCheck()));
    }

    ngOnInit(): void {
        this.init(this.menuType);
    }

    @HostListener('mouseout', ['$event'])
    public handleMouseOut(event: Event) {
        super.handleMouseOut(event);
    }

    @HostListener('mouseover', ['$event'])
    public handleMouseOver(event: Event) {
        super.handleMouseOver(event);
    }

    @HostListener('click', ['$event'])
    public handleClick(event: Event) {
        super.handleClick(event);
    }

    @HostListener('document:click', ['$event'])
    public handleDropdown(event: Event) {
        super.handleDropdown(event);
    }

    public getMenuItemClasses(item: MenuItem): string {
        const css = ClassBuilder.create(item.itemClass || '');
        if (item.items) {
            switch (this.menuType) {
                case 'dropdown':
                    css.css('menu-lg-down-accordion menu-dropdown');
                    break;
                case 'accordion':
                    css.css('menu-accordion');
                    break;
                default:
                    break;
            }
        }
        return css.toString();
    }

    public getSubMenuClasses(item: MenuItem): string {
        const css = ClassBuilder.create(item.subMenuClass || '');
        if (item.items) {
            switch (this.menuType) {
                case 'dropdown':
                    css.css('menu-sub-lg-down-accordion menu-sub-lg-dropdown');
                    break;
                case 'accordion':
                    css.css('menu-sub-accordion');
                    if (this.isMenuItemIsActive(item)) {
                        css.css('menu-active-bg show');
                    }
                    break;
                default:
                    break;
            }
        }
        return css.toString();
    }

    public getLinkClasses(item: MenuItem): string {
        const builder = ClassBuilder.create(item.linkClass || '');
        if (this.isMenuItemIsActive(item)) {
            builder.css('active');
        }
        return builder.toString();
    }

    public getLinkActive(item: MenuItem): string {
        const builder = ClassBuilder.create('active');
        if (item.items && this.menuType === 'accordion') {
            builder.css('here show');
        }
        return builder.toString();
    }

    public getTrigger(item: MenuItem): string {
        if (!item.trigger) {
            return 'click';
        }
        if (typeof item.trigger === 'string') {
            return item.trigger;
        }
        return JSON.stringify(item.trigger);
    }
    public getBulletClass(item: MenuItem): string {
        if (item.icon) return '';
        return item.bullet ? `bullet-${item.bullet}` : 'bullet-dot';
    }

    public getTitle(item: MenuItem): unknown {
        if (item.translate) {
            return this._translateService.instant(item.translate);
        }
        return item.title;
    }

    /**
     * Check Menu is active
     *
     * @param item: any
     */
    public isMenuItemIsActive(item: MenuItem): boolean {
        if (item.items) {
            return this.isSubMenuItemIsActive(item);
        }

        if (!item.url) {
            return false;
        }

        return this.router.url === item.url;
    }

    /**
     * Check Menu sub Item is active
     *
     * @param item: any
     */
    public isSubMenuItemIsActive(item: MenuItem): boolean {
        if (item.items) {
            for (const subItem of item.items) {
                if (subItem.type === 'item' && this.isMenuItemIsActive(subItem)) {
                    return true;
                }
            }
        }
        return false;
    }

    public useSVG(item: MenuItem): boolean {
        return item.icon != null && item.icon.endsWith('.svg');
    }
}
