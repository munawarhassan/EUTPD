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
                title: 'Dashboard',
                url: '/dashboard',
                translate: 'header.menu.dashboard',
                icon: 'icons/duotune/art/art002.svg',
            },
            {
                title: 'Layout Builder',
                url: '/builder',
                translate: 'header.menu.layout_builder',
                icon: 'icons/duotune/general/gen019.svg',
            },
            {
                title: 'Example',
                items: [
                    {
                        title: 'Components',
                        items: [
                            {
                                title: 'Layout',
                                items: [
                                    {
                                        title: 'Menu',
                                        url: '/apps/layout/menu',
                                    },
                                    {
                                        title: 'Pagination',
                                        url: '/apps/layout/pagination',
                                    },
                                    {
                                        title: 'Table',
                                        url: '/apps/layout/table',
                                    },
                                    {
                                        title: 'Stepper',
                                        url: '/apps/layout/stepper',
                                    },
                                    {
                                        title: 'Wizard',
                                        url: '/apps/layout/wizard',
                                    },
                                    {
                                        title: 'Portlet',
                                        url: '/apps/layout/portlet',
                                    },
                                    {
                                        title: 'Sticky Forms',
                                        url: '/apps/layout/sticky-form',
                                    },
                                ],
                            },
                            {
                                title: 'Boostrap',
                                items: [
                                    {
                                        title: 'Nav Tab',
                                        url: '/apps/layout/tab',
                                    },
                                    {
                                        title: 'Accordion',
                                        url: '/apps/layout/accordion',
                                    },
                                    {
                                        title: 'Progress Bar',
                                        url: '/apps/layout/progress-bar',
                                    },
                                ],
                            },
                            {
                                title: 'Forms',
                                items: [
                                    {
                                        title: 'Date Range Picker',
                                        url: '/apps/layout/daterange',
                                    },
                                    {
                                        title: 'Image Input',
                                        url: '/apps/layout/image-input',
                                    },
                                    {
                                        title: 'Select2',
                                        url: '/apps/layout/select2',
                                    },
                                    {
                                        title: 'Password metter',
                                        url: '/apps/layout/password-meter',
                                    },
                                    {
                                        title: 'Tagify',
                                        url: '/apps/layout/tagify',
                                    },
                                    {
                                        title: 'Control Forms',
                                        url: '/apps/layout/control-form',
                                    },
                                    {
                                        title: 'Drop Zone',
                                        url: '/apps/layout/dropzone',
                                    },
                                ],
                            },
                        ],
                    },
                    {
                        title: 'Directives',

                        items: [
                            {
                                title: 'Collapse',
                                url: '/apps/layout/collapse',
                            },
                        ],
                    },
                ],
            },
            {
                title: 'Crafted',
                translate: 'header.menu.crafted',
                items: [
                    {
                        title: 'Pages',
                        icon: 'icons/duotune/general/gen022.svg',
                        translate: 'header.menu.page',
                        items: [
                            {
                                title: 'Profile',
                                translate: 'header.menu.profile.title',
                                items: [
                                    {
                                        title: 'Overview',
                                        url: '/crafted/pages/profile/overview',
                                        translate: 'header.menu.profile.overview',
                                    },
                                    {
                                        title: 'Projects',
                                        url: '/crafted/pages/profile/projects',
                                        translate: 'header.menu.profile.project',
                                    },
                                    {
                                        title: 'Campaigns',
                                        url: '/crafted/pages/profile/campaigns',
                                        translate: 'header.menu.profile.campaign',
                                    },
                                    {
                                        title: 'Documents',
                                        url: '/crafted/pages/profile/documents',
                                        translate: 'header.menu.profile.document',
                                    },
                                    {
                                        title: 'Connections',
                                        url: '/crafted/pages/profile/connections',
                                        translate: 'header.menu.profile.connection',
                                    },
                                ],
                            },
                            {
                                title: 'Wizards',
                                translate: 'header.menu.wizard.title',
                                items: [
                                    {
                                        title: 'Horizontal',
                                        url: '/crafted/pages/wizards/horizontal',
                                        translate: 'header.menu.wizard.horizontal',
                                    },
                                    {
                                        title: 'Vertical',
                                        url: '/crafted/pages/wizards/vertical',
                                        translate: 'header.menu.wizard.vertical',
                                    },
                                ],
                            },
                        ],
                    },
                    {
                        title: 'Accounts',
                        icon: 'icons/duotune/communication/com006.svg',
                        url: '/crafted/account',
                        translate: 'header.menu.account.title',
                    },
                    {
                        title: 'Errors',
                        icon: 'icons/duotune/general/gen040.svg',
                        items: [
                            {
                                title: 'Error 404',
                                url: '/error/404',
                            },
                            {
                                title: 'Error 500',
                                url: '/error/500',
                            },
                        ],
                    },
                    {
                        title: 'Widgets',
                        icon: 'icons/duotune/general/gen025.svg',
                        items: [
                            {
                                title: 'Lists',
                                url: '/crafted/widgets/lists',
                            },
                            {
                                title: 'Statistics',
                                url: '/crafted/widgets/statistics',
                            },
                            {
                                title: 'Charts',
                                url: '/crafted/widgets/charts',
                            },
                            {
                                title: 'Mixed',
                                url: '/crafted/widgets/mixed',
                            },
                            {
                                title: 'Tables',
                                url: '/crafted/widgets/tables',
                            },
                            {
                                title: 'Feeds',
                                url: '/crafted/widgets/feeds',
                            },
                        ],
                    },
                ],
            },
            {
                title: 'Apps',
                items: [
                    {
                        title: 'Chat',
                        icon: 'icons/duotune/communication/com012.svg',
                        items: [
                            {
                                title: 'Private Chat',
                                url: '/apps/chat/private-chat',
                            },
                            {
                                title: 'Group Chart',
                                url: '/apps/chat/group-chat',
                            },
                            {
                                title: 'Drawer Chart',
                                url: '/apps/chat/drawer-chat',
                            },
                        ],
                    },
                    {
                        title: 'Invoice Management',
                        icon: 'icons/duotune/finance/fin002.svg',
                        items: [
                            {
                                title: 'View Invoices',
                                items: [
                                    {
                                        title: 'Invoice 1',
                                        url: '/apps/invoices/view/invoice1',
                                    },
                                    {
                                        title: 'Invoice 2',
                                        url: '/apps/invoices/view/invoice2',
                                    },
                                ],
                            },
                            {
                                title: 'Create Invoice',
                                url: '/apps/invoices/create',
                            },
                        ],
                    },
                ],
            },
            {
                title: 'Mega menu',
                items: [
                    {
                        title: 'Example link',
                    },
                    {
                        title: 'Example link',
                    },
                    {
                        title: 'Example link',
                    },
                    {
                        title: 'Example link',
                    },
                    {
                        title: 'Example link',
                    },
                    {
                        title: 'Example link',
                    },
                    {
                        title: 'Example link',
                    },
                    {
                        title: 'Example link',
                    },
                    {
                        title: 'Example link',
                    },
                    {
                        title: 'Example link',
                    },
                    {
                        title: 'Example link',
                    },
                ],
            },
        ],
    },
    aside: {
        type: 'accordion',
        override: {
            iconClass: 'svg-icon-1',
        },
        items: [
            {
                title: 'Dashboard',
                url: '/dashboard',
                translate: 'header.menu.dashboard',
                icon: 'icons/duotune/art/art002.svg',
            },
            {
                title: 'Layout Builder',
                url: '/builder',
                translate: 'header.menu.layout_builder',
                icon: 'icons/duotune/general/gen019.svg',
            },
            {
                title: 'Example',
                items: [
                    {
                        title: 'Components',
                        items: [
                            {
                                title: 'Layout',
                                items: [
                                    {
                                        title: 'Menu',
                                        url: '/apps/layout/menu',
                                    },
                                    {
                                        title: 'Pagination',
                                        url: '/apps/layout/pagination',
                                    },
                                    {
                                        title: 'Table',
                                        url: '/apps/layout/table',
                                    },
                                    {
                                        title: 'Stepper',
                                        url: '/apps/layout/stepper',
                                    },
                                    {
                                        title: 'Wizard',
                                        url: '/apps/layout/wizard',
                                    },
                                    {
                                        title: 'Portlet',
                                        url: '/apps/layout/portlet',
                                    },
                                    {
                                        title: 'Sticky Forms',
                                        url: '/apps/layout/sticky-form',
                                    },
                                ],
                            },
                            {
                                title: 'Boostrap',
                                items: [
                                    {
                                        title: 'Nav Tab',
                                        url: '/apps/layout/tab',
                                    },
                                    {
                                        title: 'Accordion',
                                        url: '/apps/layout/accordion',
                                    },
                                    {
                                        title: 'Progress Bar',
                                        url: '/apps/layout/progress-bar',
                                    },
                                ],
                            },
                            {
                                title: 'Forms',
                                items: [
                                    {
                                        title: 'Date Range Picker',
                                        url: '/apps/layout/daterange',
                                    },
                                    {
                                        title: 'Image Input',
                                        url: '/apps/layout/image-input',
                                    },
                                    {
                                        title: 'Select2',
                                        url: '/apps/layout/select2',
                                    },
                                    {
                                        title: 'Password metter',
                                        url: '/apps/layout/password-meter',
                                    },
                                    {
                                        title: 'Tagify',
                                        url: '/apps/layout/tagify',
                                    },
                                    {
                                        title: 'Control Forms',
                                        url: '/apps/layout/control-form',
                                    },
                                    {
                                        title: 'Drop Zone',
                                        url: '/apps/layout/dropzone',
                                    },
                                ],
                            },
                        ],
                    },
                    {
                        title: 'Directives',

                        items: [
                            {
                                title: 'Collapse',
                                url: '/apps/layout/collapse',
                            },
                        ],
                    },
                ],
            },
            {
                title: 'Crafted',
                translate: 'header.menu.crafted',
                items: [
                    {
                        title: 'Pages',
                        icon: 'icons/duotune/general/gen022.svg',
                        translate: 'header.menu.page',
                        items: [
                            {
                                title: 'Profile',
                                translate: 'header.menu.profile.title',
                                items: [
                                    {
                                        title: 'Overview',
                                        url: '/crafted/pages/profile/overview',
                                        translate: 'header.menu.profile.overview',
                                    },
                                    {
                                        title: 'Projects',
                                        url: '/crafted/pages/profile/projects',
                                        translate: 'header.menu.profile.project',
                                    },
                                    {
                                        title: 'Campaigns',
                                        url: '/crafted/pages/profile/campaigns',
                                        translate: 'header.menu.profile.campaign',
                                    },
                                    {
                                        title: 'Documents',
                                        url: '/crafted/pages/profile/documents',
                                        translate: 'header.menu.profile.document',
                                    },
                                    {
                                        title: 'Connections',
                                        url: '/crafted/pages/profile/connections',
                                        translate: 'header.menu.profile.connection',
                                    },
                                ],
                            },
                            {
                                title: 'Wizards',
                                translate: 'header.menu.wizard.title',
                                items: [
                                    {
                                        title: 'Horizontal',
                                        url: '/crafted/pages/wizards/horizontal',
                                        translate: 'header.menu.wizard.horizontal',
                                    },
                                    {
                                        title: 'Vertical',
                                        url: '/crafted/pages/wizards/vertical',
                                        translate: 'header.menu.wizard.vertical',
                                    },
                                ],
                            },
                        ],
                    },
                    {
                        title: 'Accounts',
                        icon: 'icons/duotune/communication/com006.svg',
                        url: '/crafted/account',
                    },
                    {
                        title: 'Errors',
                        icon: 'icons/duotune/general/gen040.svg',
                        items: [
                            {
                                title: 'Error 404',
                                url: '/error/404',
                            },
                            {
                                title: 'Error 500',
                                url: '/error/500',
                            },
                        ],
                    },
                    {
                        title: 'Widgets',
                        icon: 'icons/duotune/general/gen025.svg',
                        items: [
                            {
                                title: 'Lists',
                                url: '/crafted/widgets/lists',
                            },
                            {
                                title: 'Statistics',
                                url: '/crafted/widgets/statistics',
                            },
                            {
                                title: 'Charts',
                                url: '/crafted/widgets/charts',
                            },
                            {
                                title: 'Mixed',
                                url: '/crafted/widgets/mixed',
                            },
                            {
                                title: 'Tables',
                                url: '/crafted/widgets/tables',
                            },
                            {
                                title: 'Feeds',
                                url: '/crafted/widgets/feeds',
                            },
                        ],
                    },
                ],
            },
            {
                title: 'Apps',
                items: [
                    {
                        title: 'Chat',
                        icon: 'icons/duotune/communication/com012.svg',
                        items: [
                            {
                                title: 'Private Chat',
                                url: '/apps/chat/private-chat',
                            },
                            {
                                title: 'Group Chart',
                                url: '/apps/chat/group-chat',
                            },
                            {
                                title: 'Drawer Chart',
                                url: '/apps/chat/drawer-chat',
                            },
                        ],
                    },
                    {
                        title: 'Invoice Management',
                        icon: 'icons/duotune/finance/fin002.svg',
                        items: [
                            {
                                title: 'View Invoices',
                                items: [
                                    {
                                        title: 'Invoice 1',
                                        url: '/apps/invoices/view/invoice1',
                                    },
                                    {
                                        title: 'Invoice 2',
                                        url: '/apps/invoices/view/invoice2',
                                    },
                                ],
                            },
                            {
                                title: 'Create Invoice',
                                url: '/apps/invoices/create',
                            },
                        ],
                    },
                ],
            },
            {
                type: 'separator',
                class: 'mx-1 my-4',
            },
            {
                title: 'Changelog',
                url: '/changelog',
                icon: 'icons/duotune/general/gen005.svg',
            },
        ],
    },
};
