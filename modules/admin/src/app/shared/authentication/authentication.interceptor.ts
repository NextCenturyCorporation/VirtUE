import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpResponse, HttpInterceptor, HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import 'rxjs/add/operator/do'
import { map } from 'rxjs/operators';
import 'rxjs/add/operator/map'
import { _throw } from 'rxjs/observable/throw';
import { catchError } from 'rxjs/operators/catchError';


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
    // request = request.clone({
    // headers: request.headers
                              // .set('X-Requested-With', 'XMLHttpRequest')
                              // .set('withCredentials', 'true')
                              // .set('Accept', 'application/json')
                              // .set('Set-Cookie', 'text/plain')
    // });

    request = request.clone({
      // headers: request.headers.set('content-type', 'x-form-encoded'),
      // headers: request.headers.set('content-type', 'plain/text'),
      // headers: request.headers.set('content-type', 'application/json'),
      withCredentials: true
    });

    // add authorization header with jwt token if available
    // let currentUser = JSON.parse(localStorage.getItem('currentUser'));
    // if (currentUser && currentUser.token) {
    //   request = request.clone({
    //     setHeaders: {
    //         Authorization: `Bearer ${currentUser.token}`
    //     }
    //   });
    // }

    if (this.auth.isAuthenticated()) {
      // request = request.clone({
      //   setHeaders: {
      //       Authorization: `Bearer ${currentUser.token}`
      //   }
      // });
    }


    console.log(request);
    // return next.handle(request);

    return next
      .handle(request)
      .pipe(map((something) => {console.log("?");console.log(something);console.log("?"); return something}))
      .pipe(map((response: any) => {
        console.log('returned: ', response);
        // if ( !(response && response.status === 200) ) {
        //     // go to login
        //
        // }
        if ( !response || response.status === 401 ) {
            // go to login page and prevent access of anything else until you log in again. ?
            this.auth.markUnauthenticated();
        }

        return response;
      }))

      // .pipe(catchError((res: any, caught: Observable<HttpEvent<any>> ) => {
      //   console.log("**");
      //   // console.log(res);
      //   console.log(res.status);
      //   console.log("**");
      //   // if (res.status !== 200) {
      //   //   return _throw(res.status);
      //   // }
      //   // return _throw(res.status);
      //   //
      //   return this.handleError(res)
      //
      // }))
      // .do((ev: any) => {
      //   // console.log("got an event",ev)
      //   console.log("$$");
      //   console.log(ev);
      //   if (ev instanceof HttpResponse) {
      //     console.log(ev.status);
      //     // console.log('response headers', ev.headers.keys());
      //     // console.log(ev.headers.get("expires"));
      //   }
      //   console.log("$$");
      //   return ev;
      // })
      ;
  }

  public handleError(error: any) {
    console.log("{ " + error + " }");
    if (error.status === 401) {
      //
    } else if (error.status === 400) {
      //
    }
    return Promise.reject(error);
  }
}
