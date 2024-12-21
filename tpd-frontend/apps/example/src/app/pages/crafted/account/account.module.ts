import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MenuModule } from '@devacfr/layout';
import { DirectivesModule, InlineSVGModule, NavModule } from '@devacfr/bootstrap';
import { ImageInputModule, Select2Module } from '@devacfr/forms';
import { AccountApiKeysComponent } from './account-api-keys.component';
import { AccountBillingComponent } from './account-billing.component';
import { AccountOverviewComponent } from './account-overview.component';
import { AccountReferralsComponent } from './account-referrals.component';
import { AccountSecurityComponent } from './account-security.component';
import { AccountSettingsComponent } from './account-settings.component';
import { AccountStatementsComponent } from './account-statements.component';
import { AccountComponent } from './account.component';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        InlineSVGModule,
        ImageInputModule,
        NavModule,
        DirectivesModule,
        Select2Module,
        MenuModule,
        RouterModule.forChild([
            {
                path: '',
                redirectTo: 'overview',
                pathMatch: 'full',
            },
            {
                path: '',
                component: AccountComponent,
                children: [
                    {
                        path: 'overview',
                        component: AccountOverviewComponent,
                    },
                    {
                        path: 'settings',
                        component: AccountSettingsComponent,
                    },
                    {
                        path: 'security',
                        component: AccountSecurityComponent,
                    },
                    {
                        path: 'billing',
                        component: AccountBillingComponent,
                    },
                    {
                        path: 'statements',
                        component: AccountStatementsComponent,
                    },
                    {
                        path: 'referrals',
                        component: AccountReferralsComponent,
                    },
                    {
                        path: 'api-keys',
                        component: AccountApiKeysComponent,
                    },
                ],
            },
        ]),
    ],
    exports: [],
    declarations: [
        AccountComponent,
        AccountOverviewComponent,
        AccountSettingsComponent,
        AccountSecurityComponent,
        AccountBillingComponent,
        AccountStatementsComponent,
        AccountReferralsComponent,
        AccountApiKeysComponent,
    ],
    providers: [],
})
export class AccountModule {}
