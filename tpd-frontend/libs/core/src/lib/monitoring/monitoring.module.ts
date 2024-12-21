import { ModuleWithProviders, NgModule } from '@angular/core';
import { MonitoringService } from './monitoring.service';

@NgModule({
    imports: [],
    exports: [],
    declarations: [],
    providers: [],
})
export class MonitoringModule {
    public static forRoot(): ModuleWithProviders<MonitoringModule> {
        return {
            ngModule: MonitoringModule,
            providers: [MonitoringService],
        };
    }
}
