import { Component } from '@angular/core';
import { Page, Pageable } from '@devacfr/util';
import { PageChangedEvent } from '@devacfr/layout';

const data = [
    {
        name: 'Maynard Jenkins',
        company: 'SYNKGEN',
    },
    {
        name: 'Rich Barnett',
        company: 'BOILICON',
    },
    {
        name: 'Amie Bullock',
        company: 'STREZZO',
    },
    {
        name: 'Harding Campos',
        company: 'MEMORA',
    },
    {
        name: 'Cassie Vaughan',
        company: 'STRALUM',
    },
    {
        name: 'Marian Fulton',
        company: 'CORECOM',
    },
    {
        name: 'Susanne Mcknight',
        company: 'INDEXIA',
    },
    {
        name: 'Maynard Jenkins',
        company: 'SYNKGEN',
    },
    {
        name: 'Rich Barnett',
        company: 'BOILICON',
    },
    {
        name: 'Amie Bullock',
        company: 'STREZZO',
    },
    {
        name: 'Harding Campos',
        company: 'MEMORA',
    },
    {
        name: 'Cassie Vaughan',
        company: 'STRALUM',
    },
    {
        name: 'Marian Fulton',
        company: 'CORECOM',
    },
    {
        name: 'Susanne Mcknight',
        company: 'INDEXIA',
    },
    {
        name: 'Maynard Jenkins',
        company: 'SYNKGEN',
    },
    {
        name: 'Rich Barnett',
        company: 'BOILICON',
    },
    {
        name: 'Amie Bullock',
        company: 'STREZZO',
    },
    {
        name: 'Harding Campos',
        company: 'MEMORA',
    },
    {
        name: 'Cassie Vaughan',
        company: 'STRALUM',
    },
    {
        name: 'Marian Fulton',
        company: 'CORECOM',
    },
    {
        name: 'Susanne Mcknight',
        company: 'INDEXIA',
    },
];
@Component({
    selector: 'app-pagination.example',
    templateUrl: 'pagination.example.component.html',
})
export class PaginationExampleComponent {
    public page = Page.of({
        content: data,
        pageable: Pageable.of(0, 10),
    });

    public handlePageChanded(event: PageChangedEvent) {
        const pageable = event.pageable;
        this.page = Page.of({ content: data, pageable });
    }
}
