export interface PaginationOptions {
    maxSize?: number;
    itemsPerPage: number;
    rotate: boolean;
    boundaryLinks: boolean;
    directionLinks: boolean;
}

export const DefaultPaginationConfig: PaginationOptions = {
    maxSize: 20,
    rotate: false,
    itemsPerPage: 10,
    boundaryLinks: false,
    directionLinks: true,
};
