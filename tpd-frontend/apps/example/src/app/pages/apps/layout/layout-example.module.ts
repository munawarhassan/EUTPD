import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DirectivesModule, InlineSVGModule, NavModule } from '@devacfr/bootstrap';
import { PasswordInputModule, Select2Module, TagifyModule } from '@devacfr/forms';
import { MenuModule, PaginationModule, TableModule } from '@devacfr/layout';
import { MenuExampleComponent } from './menu/menu-example.component';
import { PaginationExampleComponent } from './pagination/pagination.example.component';
import { PasswordMeterExampleComponent } from './password-meter/password-meter-example.component';
import { TabExampleComponent } from './tab/tab-example.component';
import { TableExampleComponent } from './table/table-example.component';
import { TagifyExampleComponent } from './tagify/tagify-example.component';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule.forChild([
            {
                path: 'menu',
                component: MenuExampleComponent,
            },
            {
                path: 'daterange',
                loadChildren: () =>
                    import('./daterange/daterange-example.module').then((m) => m.DateRangeExampleModule),
            },
            {
                path: 'select2',
                loadChildren: () => import('./select2/select2-example.module').then((m) => m.Select2ExampleModule),
            },
            {
                path: 'accordion',
                loadChildren: () =>
                    import('./accordion/accordion-example.module').then((m) => m.AccordionExampleModule),
            },
            {
                path: 'image-input',
                loadChildren: () =>
                    import('./image-input/image-input-example.module').then((m) => m.ImageInputExampleModule),
            },
            {
                path: 'collapse',
                loadChildren: () => import('./collapse/collapse-example.module').then((m) => m.CollapseExampleModule),
            },
            {
                path: 'tab',
                component: TabExampleComponent,
            },
            {
                path: 'password-meter',
                component: PasswordMeterExampleComponent,
            },
            {
                path: 'progress-bar',
                loadChildren: () =>
                    import('./progress-bar/progress-bar-example.module').then((m) => m.ProgressBarExampleModule),
            },
            {
                path: 'pagination',
                component: PaginationExampleComponent,
            },
            {
                path: 'table',
                component: TableExampleComponent,
            },
            {
                path: 'tagify',
                component: TagifyExampleComponent,
            },
            {
                path: 'stepper',
                loadChildren: () => import('./stepper/stepper.module').then((m) => m.StepperExampleModule),
            },
            {
                path: 'wizard',
                loadChildren: () => import('./wizard/wizard-example.module').then((m) => m.WizardExampleModule),
            },
            {
                path: 'portlet',
                loadChildren: () => import('./portlet/portlet-example.module').then((m) => m.PortletExampleModule),
            },
            {
                path: 'sticky-form',
                loadChildren: () =>
                    import('./sticky-form-actions/sticky-form-actions.module').then((m) => m.SticklyPortletViewModule),
            },
            {
                path: 'control-form',
                loadChildren: () =>
                    import('./control-form/control-form-example.module').then((m) => m.ControlFormExampleModule),
            },
            {
                path: 'dropzone',
                loadChildren: () => import('./dropzone/dropzone-example.module').then((m) => m.DropzoneExampleModule),
            },
        ]),
        MenuModule,
        InlineSVGModule,
        DirectivesModule,
        Select2Module,
        NavModule,
        PasswordInputModule,
        PaginationModule,
        TableModule,
        TagifyModule,
    ],
    exports: [],
    declarations: [
        MenuExampleComponent,
        TabExampleComponent,
        PasswordMeterExampleComponent,
        PaginationExampleComponent,
        TableExampleComponent,
        TagifyExampleComponent,
    ],
    providers: [],
    schemas: [],
})
export class LayoutExampleModule {}
