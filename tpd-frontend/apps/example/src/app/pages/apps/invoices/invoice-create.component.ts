import { Component } from '@angular/core';
import moment from 'moment';

@Component({
    selector: 'app-invoice-create',
    templateUrl: 'invoice-create.component.html',
})
export class InvoiceCreateComponent {
    public invoice_date = moment().toDate();
    public due_date = moment().add(7, 'd').toDate();
}
