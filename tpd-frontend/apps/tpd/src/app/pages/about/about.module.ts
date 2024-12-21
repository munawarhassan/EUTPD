import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ModalModule } from 'ngx-bootstrap/modal';
import { AboutDialogComponent } from './about-dialog.component';

@NgModule({
    imports: [CommonModule, ModalModule],
    exports: [AboutDialogComponent],
    declarations: [AboutDialogComponent],
    providers: [],
})
export class AboutDialogModule {}
