import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpResponse, HttpInterceptor, HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import 'rxjs/add/operator/do';
import { map } from 'rxjs/operators';
import { _throw } from 'rxjs/observable/throw';
import { catchError } from 'rxjs/operators/catchError';

import { InterceptorRemoteDestinationHeader } from '../services/baseUrl.interceptor';

import { AuthenticationService } from '../services/authentication.service';

/**
Check this bug report if you're looking to fix csrf issues here:
https://github.com/angular/angular/issues/20511
Four things you need in order for angular to enable csrf protection are listed at the end.
  Not included: the withCredentials thing.
In addition to those five things, you may also need to use HttpClientXsrfModule.

The main problem is Angular won't attach the token to non-relative path urls.
For some (probably related) reason, Angular would attach the xsrf token to requests made to my local backend, but not to the
remotely-hosted backend.
 - Some places claimed setting the url's base domain in an interceptor instead of calling the endpoint directly is sufficient
to get around the relative-path restriction, but it didn't make a difference for me.
 - Other places (like below) claim that you just need to make an interceptor that pulls out the token and adds it manually.
    https://stackoverflow.com/questions/46040922/angular4-httpclient-csrf-does-not-send-x-xsrf-token
   That also didn't do anything for me. Below seems to say it simply isn't possible for Angular to access the token
   unless the destination path is actually on the same server as the request (???) - solveable only with a proxy.
    https://stackoverflow.com/questions/48002670/angular-5-unable-to-get-xsrf-token-from-httpxsrftokenextractor

This page claims sufficiency to use

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
        if ( response && response.status &&
          ((response.status === 302 ) || (response.status === 400 ) || (response.status === 401 ))) {
            // go to login page and prevent access of anything else until you log in again.
            this.auth.markUnauthenticated();
        }

        return response;
      }));
  }

}
