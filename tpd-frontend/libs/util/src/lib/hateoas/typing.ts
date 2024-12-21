export interface HateoasLink {
    rel: string;
    href: string;
}

export interface HateoasResponse<T> {
    links: HateoasLink[];
    content: T[];
    page: {
        size: number;
        totalElements: number;
        totalPages: number;
        number: number;
    };
}
