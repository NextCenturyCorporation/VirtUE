import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { map } from 'rxjs/operators';

import { AuthenticationService } from '../services/authentication.service';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
    constructor(private authenticationService: AuthenticationService) {}

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(request)
          .pipe(catchError(err => {

            // 255 means unknown error on the backend; not an authentication issue (hopefully), so don't log out.
            // That is, if you get this error, you aren't logged out on the backend, and so even if the app logs you out
            // you could get back in just by refreshing the page. So leaving off that check is no extra security,
            // and leads to weird stuff and annoyances any time there's an error on the backend.
            // Yes the backend shouldn't give errors. See console log below.
            if ((err.status >= 400) && (err.status !== 404) && (err.error.slice(12, 15) !== "255")) {
              // auto logout if 401 response returned from api
              this.authenticationService.markUnauthenticated();
              // it'd be nice if the backend returned a 401 for auth failures and a 400 for everything else, but alas,
              // it does not. It's 401 for some auth failures, 400 for others, and 400 for all other failures.
            }

            if (err.status >= 400 && err.error.slice(12, 15) === "255") {
              console.log("You may have tried to delete something that other items reference. The backend isn't " +
              "(as of late Jan 2019) set up to handle that - there should be a check on the frontend though, or at " +
              "least an informative error message.");
            }

            const error = err.message || err.statusText;
            return throwError(error);
        }))
        ;
    }
}
