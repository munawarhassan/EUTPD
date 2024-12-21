import { Params } from '@angular/router';

/**
 * Breadcrumb item built internally, private to this module
 */
export interface Breadcrumb {
    /**
     * actual route path with resolved param. Ex /mentor/2, connect/edit
     */
    routeLink?: string;
    /**
     * route with path params converted to a RegExp
     * path '/mentor/:id' becomes routeRegex '/mentor/[^/]+', which can be matched against when needed
     */
    routeRegex?: string;
    /**
     * This is additional info on each breadcrumb item whether label is auto generated or user specified
     * isAutoGeneratedLabel has to be present at component level but not at the service,
     * since we may need to support multiple breadcrumb components in same app
     */
    isAutoGeneratedLabel?: boolean;
    /**
     * Query params in string form.
     */
    queryParams?: Params;
    fragment?: string;
    routeInterceptor?: (routeLink: string | undefined, breadcrumb: Breadcrumb) => string;
}

/**
 * Breadcrumb config provided as part of App Route Config
 */
export interface BreadcrumbObject {
    /**
     * breadcrumb label for a route
     */
    label?: string | BreadcrumbFunction;
    /**
     * breadcrumb title
     */
    title?: string;

    icon?: string;

    /**
     * unique alias name for a route path that can be used to dynamically update a route's breadcrumb label via breadcrumbService.set()
     */
    alias?: string;
    /**
     * hide or show the breadcrumb item
     */
    skip?: boolean;
    /**
     * disable a certain breadcrumb in the list. Not clickable.
     * It may be needed when the routing has the path, but navigating to that path is of no use
     */
    disable?: boolean;
    /**
     * Interceptor for breadcrumb click action that returns the dynamic path
     * Consumers can change the breadcrumb routing dynamically with this approach
     */
    routeInterceptor?: (routeLink: string | undefined, breadcrumb: Breadcrumb) => string;
}

// resolved label for a route can further be enhanced based on a function
export type BreadcrumbFunction = (resolvedLabel?: string) => string;