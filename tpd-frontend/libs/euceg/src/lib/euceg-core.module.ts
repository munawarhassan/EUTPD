import { ModuleWithProviders, NgModule } from '@angular/core';
import { AuthModule } from '@devacfr/auth';
import { BACKEND_SERVER_API_URL_TOKEN, BACKEND_SERVER_URL_TOKEN } from '../shared';
import { DomibusService } from './api';
import { AttachmentService } from './attachment.service';
import { EucegStatisticService } from './euceg-statistic.service';
import { EucegService } from './euceg.service';
import { ProductService } from './product.service';
import { SubmissionActivity } from './submission-activity';
import { SubmissionService } from './submission.service';
import { SubmitterService } from './submitter-service';
import { SubmissionReportService } from './submission-report.service';

@NgModule({
    imports: [AuthModule],
    exports: [],
    declarations: [],
    providers: [],
})
export class EucegCoreModule {
    public static forRoot(backenServerApiUrl: string, backenServerUrl: string): ModuleWithProviders<EucegCoreModule> {
        return {
            ngModule: EucegCoreModule,
            providers: [
                AttachmentService,
                SubmitterService,
                ProductService,
                SubmissionService,
                SubmissionReportService,
                EucegService,
                SubmissionActivity,
                DomibusService,
                EucegStatisticService,
                {
                    provide: BACKEND_SERVER_API_URL_TOKEN,
                    useValue: backenServerApiUrl,
                },
                {
                    provide: BACKEND_SERVER_URL_TOKEN,
                    useValue: backenServerUrl,
                },
            ],
        };
    }
}
