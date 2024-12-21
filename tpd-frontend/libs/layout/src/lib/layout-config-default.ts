import { LayoutConfig } from './layout-config';

export const DefaultLayoutConfig: LayoutConfig = {
    auth: {
        componentName: 'auth',
        logo: './assets/media/logos/logo.svg',
        backgroundImage: './assets/media/bg/bg-2.jpg',
    },
    main: {
        componentName: 'main',
        type: 'default',
        primaryColor: '#009EF7',
        darkSkinEnabled: true,
    },
    loader: {
        componentName: 'loader',
        display: true,
        type: 'default', // Set default|spinner-message|spinner-logo to hide or show page loader
    },
    scrolltop: {
        componentName: 'scroll-top',
        display: true,
    },
    header: {
        logo: './assets/media/logos/logo.svg',
        componentName: 'header',
        display: true, // Set true|false to show or hide Header
        width: 'fluid', // Set fixed|fluid to change width type
        fixed: {
            desktop: true, // Set true|false to set fixed Header for desktop mode
            tabletAndMobile: true, // Set true|false to set fixed Header for tablet and mobile modes
        },
    },
    megaMenu: {
        componentName: 'mega-menu',
        display: true, // Set true|false to show or hide Mega Menu
    },
    aside: {
        componentName: 'aside',
        display: true,
        theme: 'dark',
        menu: 'main',
        fixed: true,
        minimized: false,
        minimize: true,
        hoverable: true,
    },
    content: {
        componentName: 'content',
        width: 'fixed', // Set fixed|fluid to change width
        layout: 'default',
    },
    toolbar: {
        componentName: 'toolbar',
        display: true, // Display toolbar
        width: 'fluid', // Set fixed|fluid to change width type,
        fixed: {
            desktop: true,
            tabletAndMobileMode: true,
        },
    },
    footer: {
        componentName: 'footer',
        width: 'fluid', // Set fixed|fluid to change width type
    },
};
