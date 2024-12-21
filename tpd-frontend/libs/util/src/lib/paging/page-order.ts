import { PageDirection } from './typing';

export interface Order {
    direction: PageDirection;
    property: string;
    ignoreCase: boolean;
    nullHandling: 'NATIVE' | 'NULLS_FIRST' | 'NULLS_LAST';
    ascending: boolean;
    descending: boolean;

    setIngoreCase(): Order;
}

// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace Order {
    export function by(direction: PageDirection, ...properties: string[]): Order[] {
        return properties.map((prop) => new OrderImpl(prop, direction));
    }

    export function of(direction: PageDirection, property: string): Order {
        return new OrderImpl(property, direction);
    }

    export function unsorted(): Order[] {
        return [];
    }
}

class OrderImpl implements Order {
    constructor(
        public property: string,
        public direction: PageDirection,
        public ignoreCase = false,
        public nullHandling: 'NATIVE' | 'NULLS_FIRST' | 'NULLS_LAST' = 'NATIVE'
    ) {}

    public setIngoreCase(): Order {
        this.ignoreCase = true;
        return this;
    }

    public get ascending(): boolean {
        return this.direction === 'ASC';
    }

    public get descending(): boolean {
        return this.direction === 'DESC';
    }
}
