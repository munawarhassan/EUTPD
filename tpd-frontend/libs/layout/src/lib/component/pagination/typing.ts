import { Pageable } from '@devacfr/util';

export interface PageChangedEvent {
    target: unknown;
    pageable: Pageable;
}
