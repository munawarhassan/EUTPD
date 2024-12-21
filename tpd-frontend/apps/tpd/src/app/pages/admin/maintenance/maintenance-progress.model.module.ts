import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { InlineSVGModule, ProgressBarModule } from '@devacfr/bootstrap';
import { MaintenanceService } from '@devacfr/core';
import { ModalModule } from 'ngx-bootstrap/modal';
import { MaintenanceProgressModalComponent } from './maintenance-progress.modal.component';

@NgModule({
    imports: [CommonModule, ModalModule, ProgressBarModule, InlineSVGModule],
    exports: [MaintenanceProgressModalComponent],
    declarations: [MaintenanceProgressModalComponent],
    providers: [MaintenanceService],
})
export class MaintenanceProgressModalModule {}
