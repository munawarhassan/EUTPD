import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ModalModule } from 'ngx-bootstrap/modal';
import { TeamDialogComponent } from './team-dialog.component';

@NgModule({
    imports: [CommonModule, ModalModule],
    exports: [TeamDialogComponent],
    declarations: [TeamDialogComponent],
    providers: [],
})
export class TeamDialogModule {}
