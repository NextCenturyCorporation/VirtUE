import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpResponse, HttpInterceptor, HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import 'rxjs/add/operator/do'
import { map } from 'rxjs/operators';
import { _throw } from 'rxjs/observable/throw';
import { catchError } from 'rxjs/operators/catchError';

import { InterceptorRemoteDestinationHeader } from '../services/baseUrl.interceptor';

import { AuthenticationService } from '../services/authentication.service';

/**
Check this bug report if you're looking to fix csrf issues here:
https://github.com/angular/angular/issues/20511
Four things you need in order for angular to enable csrf protection are listed at the end. Not included: the withCredentials thing.
In addition to those five things, you may also need to use HttpClientXsrfModule.

will need this later if we decide to switch over to JWTs (would prevent csrf, bypassing the current issues)
https://medium.com/@ryanchenkie_40935/angular-authentication-using-the-http-client-and-http-interceptors-2f9d1540eb8
-- section titled "Looking for Unauthorized Responses"
*/

@Injectable()
export class AuthenticationInterceptor implements HttpInterceptor {

  constructor(public auth: AuthenticationService) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    request = request.clone({
      withCredentials: true
    });

    return next
      .handle(request)
      .pipe(map((response: any) => {
        if ( response && response.status && (response.status === 302 ) || (response.status === 400 ) || (response.status === 401 )) {
            // go to login page and prevent access of anything else until you log in again.
            this.auth.markUnauthenticated();
        }

        return response;
      }));
  }

}
