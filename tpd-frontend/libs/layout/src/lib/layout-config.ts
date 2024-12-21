export interface BaseLayoutConfig {
    componentName: string;
}

export interface LoaderConfig extends BaseLayoutConfig {
    display?: boolean;
    type: 'default' | 'spinner-message' | 'spinner-logo';
}

export interface ScrollTopConfig extends BaseLayoutConfig {
    display: boolean;
}

export interface HeaderConfig extends BaseLayoutConfig {
    display: boolean;
    logo: string;
    width: 'fixed' | 'fluid';
    fixed: {
        desktop: boolean;
        tabletAndMobile: boolean;
    };
}

export interface MegaMenuConfig extends BaseLayoutConfig {
    display: boolean;
}

export interface AsideConfig extends BaseLayoutConfig {
    display: boolean; // Display aside
    theme: 'dark' | 'light'; // Set aside theme(dark|light)
    menu: 'main' | 'documentation'; // Set aside menu(main|documentation)
    fixed: boolean; // Enable aside fixed mode
    minimized: boolean; // Set aside minimized by default
    minimize: boolean; // Allow aside minimize toggle
    hoverable: boolean; // Allow aside hovering when minimized
}

export interface ContentConfig extends BaseLayoutConfig {
    width: 'fixed' | 'fluid';
    layout: 'default' | 'docs';
}

export interface FooterConfig extends BaseLayoutConfig {
    width: 'fixed' | 'fluid';
}

export interface SidebarConfig extends BaseLayoutConfig {
    display: boolean;
    toggle: boolean;
    shown: boolean;
    content: 'general' | 'user' | 'shop';
    bgColor: 'bg-white' | 'bg-info';
    displayFooter: boolean;
    displayFooterButton: boolean;
}

export interface ToolbarConfig extends BaseLayoutConfig {
    display: boolean;
    width: 'fixed' | 'fluid';
    fixed: {
        desktop: boolean; // Set fixed header for desktop
        tabletAndMobileMode: boolean; // Set fixed header for talet & mobile
    };
}

export interface MainConfig extends BaseLayoutConfig {
    body?: {
        backgroundImage?: string;
        class?: string;
    };
    primaryColor: string;
    darkSkinEnabled: boolean;
    type: 'blank' | 'default' | 'none';
}

export interface AuthConfig extends BaseLayoutConfig {
    backgroundImage: string;
    logo: string;
}

export interface LayoutConfig {
    auth: AuthConfig;
    loader: LoaderConfig;
    scrolltop: ScrollTopConfig;
    header: HeaderConfig;
    megaMenu: MegaMenuConfig;
    aside: AsideConfig;
    content: ContentConfig;
    toolbar: ToolbarConfig;
    footer: FooterConfig;
    sidebar?: SidebarConfig;
    main?: MainConfig;
}
