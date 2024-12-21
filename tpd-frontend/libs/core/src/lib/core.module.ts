import { ModuleWithProviders, NgModule } from '@angular/core';
import { StartupService } from './startup';
import { InfoService } from './info';
import { UserAdminService, UserService } from './user';
import { ConfigService } from './setting';
import { TrackerService } from './tracker';
import { httpInterceptorProviders } from './interceptors';
import { BACKEND_SERVER_API_URL_TOKEN, BACKEND_SERVER_URL_TOKEN } from './shared';

@NgModule({
    imports: [],
    exports: [],
    declarations: [],
})
export class CoreModule {
    public static forRoot(backenServerUrl: string, backenServerApiUrl: string): ModuleWithProviders<CoreModule> {
        return {
            ngModule: CoreModule,
            providers: [
                InfoService,
                StartupService,
                UserService,
                UserAdminService,
                ConfigService,
                TrackerService,
                httpInterceptorProviders,
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
