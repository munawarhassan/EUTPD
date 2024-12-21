import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { PageNotFoundComponent } from './not-found.component';

@NgModule({
    imports: [CommonModule, RouterModule.forChild([{ path: '', component: PageNotFoundComponent }])],
    declarations: [PageNotFoundComponent],
})
export class NotFoundViewModule {}
