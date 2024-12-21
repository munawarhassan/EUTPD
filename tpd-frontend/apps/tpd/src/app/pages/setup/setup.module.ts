import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AccordionModule, DirectivesModule } from '@devacfr/bootstrap';
import { SetupService } from '@devacfr/core';
import { FormControlModule } from '@devacfr/forms';
import { LayoutModule, PortletModule, WizardModule } from '@devacfr/layout';
import { TranslateModule } from '@ngx-translate/core';
import { CreateAdminComponent } from './setup-admin.component';
import { SetupConfirmationComponent } from './setup-confirmation.component';
import { SetupDatabaseComponent } from './setup-database.component';
import { SetupGeneralComponent } from './setup-general.component';
import { SetupMailComponent } from './setup-mail.component';
import { SetupComponent } from './setup.component';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: SetupComponent,
            },
        ]),
        TranslateModule,
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        WizardModule,
        LayoutModule,
        DirectivesModule,
        PortletModule,
        FormControlModule,
        AccordionModule.forRoot(),
    ],
    declarations: [
        SetupComponent,
        SetupGeneralComponent,
        SetupDatabaseComponent,
        CreateAdminComponent,
        SetupMailComponent,
        SetupConfirmationComponent,
    ],
    providers: [SetupService],
})
export class SetupViewModule {}
