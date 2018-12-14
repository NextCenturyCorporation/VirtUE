import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
will need this later probably
https://medium.com/@ryanchenkie_40935/angular-authentication-using-the-http-client-and-http-interceptors-2f9d1540eb8
-- section titled "Looking for Unauthorized Responses"
*/

@Injectable()
export class AuthenticationInterceptor implements HttpInterceptor {
    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        // add authorization header with jwt token if available
        // let currentUser = JSON.parse(localStorage.getItem('currentUser'));
        // if (currentUser && currentUser.token) {
          request = request.clone({
            // setHeaders: {
            //     Authorization: `Bearer ${currentUser.token}`
            // },
            headers: request.headers.set('X-Requested-With', 'XMLHttpRequest').set('withCredentials', 'true')
          });
        // }

        return next.handle(request);
    }
    // intercept(req: HttpRequest<any>, next: HttpHandler) {
    //   const xhr = req.clone({
    //     headers: req.headers.set('X-Requested-With', 'XMLHttpRequest')
    //   });
    //   return next.handle(xhr);
    // }
}
