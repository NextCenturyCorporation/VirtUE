import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpResponse, HttpInterceptor } from '@angular/common/http';
import { Observable } from 'rxjs';
import 'rxjs/add/operator/do'


import { AuthenticationService } from '../services/authentication.service';

/**
will need this later probably
https://medium.com/@ryanchenkie_40935/angular-authentication-using-the-http-client-and-http-interceptors-2f9d1540eb8
-- section titled "Looking for Unauthorized Responses"
*/

@Injectable()
export class AuthenticationInterceptor implements HttpInterceptor {

  constructor(public auth: AuthenticationService) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // console.log(request.keys());
    request = request.clone({
      headers: request.headers.set('X-Requested-With', 'XMLHttpRequest')
                              .set('withCredentials', 'true')
                              .set('Accept', 'application/json')
                              .set('content-type', 'text/plain')
                              // .set('X-XSRF', 'XSRF-TOKEN')
                              .set('XSRF-TOKEN', 'XSRF-TOKEN')
                              .set('Set-Cookie', 'jsessionid=?')
    });
    request = request.clone({
      withCredentials: true
    });

    // add authorization header with jwt token if available
    console.log(localStorage);
    let currentUser = JSON.parse(localStorage.getItem('currentUser'));
    if (currentUser && currentUser.token) {
      request = request.clone({
        setHeaders: {
            Authorization: `Bearer ${currentUser.token}`
        }
      });
    }

    console.log(request);
    return next
      .handle(request)
      .do((ev: HttpEvent<any>) => {
        // console.log("got an event",ev)
        if (ev instanceof HttpResponse) {
          console.log('response headers', ev.headers.keys());
        }
      });
    // return next.handle(request);
  }

}
