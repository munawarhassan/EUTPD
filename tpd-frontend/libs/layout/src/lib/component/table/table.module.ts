import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DirectivesModule } from '@devacfr/bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { PaginationModule } from '../pagination';
import { TableComponent } from './table.component';
import { NgScrollbarModule } from 'ngx-scrollbar';
import { CdkScrollableModule } from '@angular/cdk/scrolling';
import { InfiniteScrollModule } from 'ngx-infinite-scroll';
@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        PaginationModule,
        CdkScrollableModule,
        TranslateModule,
        DirectivesModule,
        NgScrollbarModule,
        InfiniteScrollModule,
    ],
    declarations: [TableComponent],
    exports: [TableComponent],
})
export class TableModule {}
