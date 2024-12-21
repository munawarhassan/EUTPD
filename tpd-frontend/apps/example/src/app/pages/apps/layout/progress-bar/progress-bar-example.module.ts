import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ProgressBarModule } from '@devacfr/bootstrap';
import { ProgressBarExampleComponent } from './progress-bar-example.component';

@NgModule({
    imports: [
        CommonModule,
        RouterModule.forChild([
            {
                path: '',
                component: ProgressBarExampleComponent,
            },
        ]),
        ProgressBarModule,
    ],
    exports: [RouterModule],
    declarations: [ProgressBarExampleComponent],
    providers: [],
})
export class ProgressBarExampleModule {}
