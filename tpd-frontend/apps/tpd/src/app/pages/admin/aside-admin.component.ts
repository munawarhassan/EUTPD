import { Component } from '@angular/core';
import { DrawerOptions, SvgIcons } from '@devacfr/bootstrap';
import { MenuComponentType, MenuConfigService } from '@devacfr/layout';

@Component({
    selector: 'app-aside-admin',
    templateUrl: './aside-admin.component.html',
})
export class AsideAdminComponent {
    public asideDrawerOptions: Partial<DrawerOptions> = {
        name: 'aside',
        activate: { default: true, lg: false },
        overlay: true,
        width: '300px',
        direction: 'start',
        toggle: '#lt_aside_mobile_toggle',
    };

    public menu: MenuComponentType = MenuConfigService.applyMenuDefault({
        type: 'accordion',
        override: {
            iconClass: 'svg-icon-1',
        },
        items: [
            {
                translate: 'global.menu.admin.overview',
                title: 'Overview',
                url: './',
                routerLinkActiveOptions: {
                    exact: true,
                },
                icon: this.svgIcons.Simple.communication.dialNumbers,
            },
            {
                translate: 'global.menu.admin.accounts',
                title: 'Accounts',
                items: [
                    {
                        translate: 'global.menu.admin.users',
                        title: 'Users',
                        url: 'users',
                        icon: this.svgIcons.Simple.general.user,
                        routerLinkActiveOptions: {
                            exact: false,
                        },
                    },
                    {
                        translate: 'global.menu.admin.groups',
                        title: 'Groups',
                        url: 'groups',
                        icon: this.svgIcons.Simple.communication.group,
                        routerLinkActiveOptions: {
                            exact: false,
                        },
                    },
                    {
                        translate: 'global.menu.admin.globalpermissions',
                        title: 'Global permission',
                        url: 'globalpermissions',
                        icon: this.svgIcons.Simple.communication.shieldUser,
                    },
                    {
                        translate: 'global.menu.admin.ldap',
                        title: 'LDAP Server',
                        url: 'ldap',
                        icon: this.svgIcons.Simple.communication.addressBookCard,
                    },
                ],
            },
            {
                translate: 'global.menu.admin.settings',
                title: 'Settings',
                icon: this.svgIcons.Simple.code.settings4,
                items: [
                    {
                        translate: 'global.menu.admin.server',
                        title: 'Server settings',
                        url: 'server',
                        icon: this.svgIcons.Simple.devices.server,
                    },
                    {
                        translate: 'global.menu.admin.database',
                        title: 'Database',
                        url: 'database',
                        icon: this.svgIcons.Simple.devices.hardDrive,
                        routerLinkActiveOptions: {
                            exact: false,
                        },
                    },
                    {
                        translate: 'global.menu.admin.index',
                        title: 'Search indexes',
                        url: 'indexing',
                        icon: this.svgIcons.Simple.code.git4,
                    },
                    {
                        translate: 'global.menu.admin.mail',
                        title: 'Mails server',
                        url: 'mail',
                        icon: this.svgIcons.Simple.communication.incomingBox,
                    },
                    {
                        translate: 'global.menu.admin.keystore',
                        title: 'Key Store',
                        url: 'keystore',
                        icon: this.svgIcons.Simple.general.shieldCheck,
                    },
                    {
                        translate: 'global.menu.admin.domibus',
                        title: 'Domibus Server',
                        url: 'domibus',
                        icon: this.svgIcons.Simple.electric.socketEU,
                    },
                ],
            },
            {
                translate: 'global.menu.admin.support',
                title: 'Support',
                items: [
                    {
                        translate: 'global.menu.admin.api',
                        title: 'API',
                        url: 'api',
                        icon: this.svgIcons.Simple.shopping.box2,
                    },
                    {
                        translate: 'global.menu.admin.metrics',
                        title: 'Metrics',
                        url: 'metrics',
                        icon: this.svgIcons.Simple.devices.diagnostics,
                    },
                    {
                        translate: 'global.menu.admin.console',
                        title: 'Console',
                        url: 'console',
                        icon: this.svgIcons.Simple.code.terminal,
                    },
                    {
                        translate: 'global.menu.admin.audits',
                        title: 'Audits',
                        url: 'audits',
                        icon: this.svgIcons.Simple.tools.angleGrinder,
                    },
                    {
                        translate: 'global.menu.admin.health',
                        title: 'Health',
                        url: 'health',
                        icon: this.svgIcons.Simple.general.heart,
                    },
                    {
                        translate: 'global.menu.admin.tracker',
                        title: 'User Tracker',
                        url: 'tracker',
                        icon: this.svgIcons.Simple.general.binocular,
                    },
                ],
            },
        ],
    });

    constructor(public svgIcons: SvgIcons) {}
}
