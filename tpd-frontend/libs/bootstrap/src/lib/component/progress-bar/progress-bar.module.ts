import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ProgressBarItemComponent } from './progress-bar-item.component';

import { ProgressBarComponent } from './progress-bar.component';

@NgModule({
    imports: [CommonModule],
    exports: [ProgressBarComponent, ProgressBarItemComponent],
    declarations: [ProgressBarComponent, ProgressBarItemComponent],
})
export class ProgressBarModule {}
