import { mSvgIcons } from '@devacfr/bootstrap';
import { MenuConfig } from '@devacfr/layout';

export const DefaultMenuConfig: MenuConfig = {
    header: {
        type: 'dropdown',
        override: {
            iconClass: 'svg-icon-2',
            itemClass: 'me-lg-1',
            linkClass: 'py-3',
            subMenuClass: 'menu-rounded-0 py-lg-4 w-lg-225px',
        },
        items: [
            {
                title: 'Home',
                url: '/home',
                icon: mSvgIcons.Duotone.general.home,
                permission: 'USER',
            },
            {
                title: 'Product',
                url: '/product',
                permission: 'USER',
                trigger: { default: 'click', lg: 'hover' },
                icon: mSvgIcons.Simple.shopping.box3,
                items: [
                    {
                        translate: 'euceg.menu.product.submitters',
                        title: 'Submitters',
                        url: '/product/submitters',
                        icon: 'flaticon2-avatar',
                        permission: 'USER',
                    },
                    {
                        translate: 'euceg.menu.product.tobacco-products',
                        title: 'Tobacco Products',
                        url: '/product/tobacco-products',
                        icon: 'flaticon2-box-1',
                        permission: 'USER',
                    },
                    {
                        translate: 'euceg.menu.product.e-cigarettes',
                        title: 'E-Cigarettes',
                        url: '/product/ecig-products',
                        icon: 'flaticon2-box-1',
                        permission: 'USER',
                    },
                    {
                        translate: 'euceg.menu.product.attachments',
                        title: 'Attachments',
                        url: '/product/attachments',
                        icon: 'flaticon2-files-and-folders',
                        permission: 'USER',
                    },
                    {
                        translate: 'euceg.menu.product.submissions',
                        title: 'Submissions',
                        url: '/product/submissions',
                        icon: 'flaticon2-box',
                        permission: 'USER',
                    },
                ],
            },
            {
                title: 'Administration',
                permission: 'ADMIN',
                url: '/admin',
                icon: mSvgIcons.Simple.communication.dialNumbers,
            },
        ],
    },
};
