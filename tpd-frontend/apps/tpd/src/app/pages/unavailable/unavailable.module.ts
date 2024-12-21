import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LayoutModule } from '@devacfr/layout';
import { UnavailableErrorComponent } from './unavailable.component';

const routes: Routes = [{ path: '', component: UnavailableErrorComponent }];

@NgModule({
    imports: [CommonModule, LayoutModule, RouterModule.forChild(routes)],
    declarations: [UnavailableErrorComponent],
})
export class UnavailableViewModule {}
