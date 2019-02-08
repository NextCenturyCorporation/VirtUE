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
            if (err.status >= 400) {
              let errCode = this.getErrorCode(err.error);

              if ((err.status !== 404) && this.authFailure(errCode)) {
                // auto logout if 401 response returned from api
                this.authenticationService.markUnauthenticated();
                // it'd be nice if the backend returned a 401 for auth failures and a 400 for everything else, but alas,
                // it does not. It's 401 for some auth failures, 400 for others, and 400 for all other failures.
                return throwError(err);
              }

              if (errCode === 255) {
                let msg = "You may have tried to delete something that other items reference. The backend isn't " +
                "currently set up to handle that. ";
                alert(msg);
                console.log(msg);
              }

              if (errCode === 300) {
                alert(err.error);
                return new Observable<HttpEvent<any>>( () => err);

              }
              return throwError(err);
            }
            return throwError(err);
        }))
        ;
    }

    authFailure(errCode: number): boolean {
      let badAuthCodes = [
        2,
        3,
        4,
        24 // I feel like this shouldn't be an auth failure, but right now the backend uses it for all unauthorized requests
      ];
      return badAuthCodes.includes(errCode);
    }

    /**
     * This assumes the error message has the form:
     *    text: {value} other text\n more lines
     * I.e.
     *    Error Code: 24 - User not found\nException Message: User=anonymousUser was not found
     *
     */
    getErrorCode(errMsg: string): number {
      let firstLine = errMsg.split("\n")[0];
      if (firstLine.split(":").length === 0) {
        return -1;
      }

      return Number(firstLine.split(":")[1].trim().split(" ")[0]);
    }
}
