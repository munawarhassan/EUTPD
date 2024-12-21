import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '@devacfr/auth';

@Component({
    selector: 'app-logout',
    template: '',
    encapsulation: ViewEncapsulation.None,
})
export class LogoutComponent implements OnInit {
    constructor(private _router: Router, private _authService: AuthService) {}

    public ngOnInit(): void {
        // reset login status
        this._authService.logout().subscribe(() => {
            this._router.navigate(['/']);
        });
    }
}
