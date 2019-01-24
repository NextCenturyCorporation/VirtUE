import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

import { AuthenticationService } from '../services/authentication.service';

@Injectable()
export class LoginGuard implements CanActivate {

  constructor(private router: Router, private authenticationService: AuthenticationService) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    // If they're logged in, redirect them to somewhere besides the log in page, so they don't get used to having to log
    // in unnecessarily.
    if ( ! this.authenticationService.isAuthenticated()) {
        return true;
    }

    // not logged in, so redirect to login page with the return url
    this.router.navigate(['/dashboard']);
    return false;
  }
}
