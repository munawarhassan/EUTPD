import { Component, ViewChild } from '@angular/core';
import { WizardComponent, WizardMode } from '@devacfr/layout';
import { ElementAnimateUtil } from '@devacfr/util';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-wizard-example',
    templateUrl: './wizard-example.component.html',
})
export class WizardExampleComponent {
    public mode: WizardMode = 'tab';

    public user = {
        detail: {
            name: 'Nick Stone',
            email: 'nick.stone@gmail.com',
            phone: '1-541-754-3010',
            address1: 'Headquarters 1120 N Street Sacramento 916-654-5266',
            address2: 'P.O. Box 942873 Sacramento, CA 94273-0001',
            city: 'Polo Alto',
            state: 'California',
            country: 'US',
        },
        account: {
            url: 'http://sinortech.vertoffice.com',
            username: 'nick.stone',
            password: 'qwerty',
        },
        settings: {
            group: 2,
            communications: ['email'],
        },
        billing: {
            cardholderName: 'Nick Stone',
            cardNumber: '372955886840581',
            expirationMonth: '04',
            expirationYear: '2021',
            cvv: '450',
            address1: 'Headquarters 1120 N Street Sacramento 916-654-5266',
            address2: 'P.O. Box 942873 Sacramento, CA 94273-0001',
            city: 'Polo Alto',
            state: 'California',
            zip: '34890',
            country: 'US',
            delivery: 1,
        },
    };

    @ViewChild(WizardComponent)
    public wizard: WizardComponent | undefined;

    public handleChange() {
        ElementAnimateUtil.scrollTop(0, 600);
    }

    public handleComplete() {
        Swal.fire({
            title: '',
            text: 'The application has been successfully submitted!',
            icon: 'success',
        });
    }
}
