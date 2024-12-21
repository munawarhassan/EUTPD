import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Select2Module } from '@devacfr/forms';
import { Select2ExampleComponent } from './select2-example.component';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule.forChild([
            {
                path: '',
                component: Select2ExampleComponent,
            },
        ]),
        Select2Module,
    ],
    exports: [RouterModule],
    declarations: [Select2ExampleComponent],
    providers: [],
})
export class Select2ExampleModule {}
