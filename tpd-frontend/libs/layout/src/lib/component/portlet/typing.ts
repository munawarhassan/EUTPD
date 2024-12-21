/**
 *
 */
export interface Portlet {
    /**
     *
     */
    get collapsed(): boolean;

    /**
     *
     */
    toggle(): void;

    /**
     *
     */
    reloadPortlet(): void;
}

export type PortletToolType = 'toggle' | 'fullscreen' | 'reload' | 'remove';

export type PortletModeType =
    | 'fit'
    | 'bordered'
    | 'bordered-semi'
    | 'head-lg'
    | 'head-xl'
    | 'mobile'
    | 'height-fluid'
    | 'head-noborder'
    | 'rounded'
    | 'unelevate'
    | 'tabs'
    | 'solid-dark'
    | 'solid-primary'
    | 'solid-warning'
    | 'solid-danger'
    | 'solid-info';
export type PortletFooterModeType = 'sm' | 'md' | 'no-border' | 'top' | 'solid' | 'fit';
export type PortletBodyModeType =
    | 'center'
    | 'center-x'
    | 'center-y'
    | 'hor-fit'
    | 'stick-bottom'
    | 'fluid' //
    | 'fill'
    | 'unfill'
    | 'fullheight'
    | 'fit'
    | 'fit-top'
    | 'fit-bottom'
    | 'fit-x'
    | 'fit-y';

export type PortletHeadModeType = 'noborder' | 'sm' | 'lg' | 'xl' | 'fit' | 'right';
export interface PortletEvent {
    target: Portlet;
}

export type PortletOption = {
    sticky: {
        offset: number | (() => number);
        zIndex: number;
    };
    bodyToggleSpeed: number;
    tooltips: boolean;
    tools:
        | Record<PortletToolType, any>
        | {
              toggle: {
                  collapse: string;
                  expand: string;
              };
              reload: string;
              remove: string;
              fullscreen: {
                  off: string;
                  on: string;
              };
          };
};

export const DefaultPortletOptions: PortletOption = {
    sticky: {
        offset: (): number => {
            const height = $('lt_header').height();
            return height ? height : 0;
        },
        zIndex: 90,
    },
    bodyToggleSpeed: 400,
    tooltips: true,
    tools: {
        toggle: {
            collapse: 'Collapse',
            expand: 'Expand',
        },
        reload: 'Reload',
        remove: 'Remove',
        fullscreen: {
            off: 'Fullscreen',
            on: 'Exit Fullscreen',
        },
    },
};
