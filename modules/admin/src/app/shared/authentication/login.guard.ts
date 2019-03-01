import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

import { AuthenticationService } from '../services/authentication.service';

@Injectable()
export class LoginGuard implements CanActivate {

  constructor(private router: Router, private authenticationService: AuthenticationService) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    if ( ! this.authenticationService.isAuthenticated()) {
        return true;
    }

    // If they're logged in, redirect them to somewhere besides the log in page, so they don't get used to having to log
    // in unnecessarily.
    this.router.navigate(['/dashboard']);
    return false;
  }
}
